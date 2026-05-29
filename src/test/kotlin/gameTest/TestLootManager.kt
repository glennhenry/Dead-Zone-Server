package gameTest

import encore.definition.GameReference
import game.definition.ItemsDefinition
import game.definition.ItemsXmlDataLoader
import game.definition.XmlDataSource
import game.domain.mission.LootGenerator
import game.domain.mission.model.LootParameter
import game.socket.handler.save.mission.response.loadSceneXML
import org.w3c.dom.Element
import java.io.BufferedWriter
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestLootManager {
    private val PARAMETER1 = LootParameter(
        areaLevel = 30,
        playerLevel = 30,
        itemWeightOverrides = mapOf(),
        specificItemBoost = mapOf(
            "fuel-cans" to 3.0 // +300% find fuel chance (of the base chance)
        ),
        itemTypeBoost = mapOf(
            "junk" to 0.8 // +80% junk find chance
        ),
        itemQualityBoost = mapOf(
            "blue" to 0.5 // +50% blue quality find chance
        ),
        baseWeight = 1.0,
        fuelLimit = 50
    )
    private val SCENES_TO_TEST = listOf("exterior-bridge-1")
    private val GENERATION_PER_SCENE = 10

    val logDir = File(".logs")
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
    val logFile = File(logDir, "loot_generation_test-$timestamp.log")

    @BeforeTest
    fun setup() {
        GameReference.initialize {
            add(XmlDataSource("assets/game/data/xml/items.xml.gz"), ItemsXmlDataLoader())
        }
    }

    @Test
    fun testLootGeneration() {
        if (!logDir.exists()) logDir.mkdirs()

        val itemsDefinition = GameReference.get<ItemsDefinition>()

        // output to html
        val htmlFile = File(logDir, "loot_generation_report.html")
        htmlFile.bufferedWriter().use { writer ->
            prepareHtmlTemplate(writer)

            SCENES_TO_TEST.forEach { filename ->
                repeat(GENERATION_PER_SCENE) { i ->
                    val sceneXML = loadSceneXML(filename)
                    val manager = LootGenerator(
                        sceneXML = sceneXML,
                        parameter = PARAMETER1
                    )
                    manager.insertLoots()

                    val lootsAggregate: Map<String, Int> = manager.insertedLoots
                        .groupingBy { it.itemIdInXML }
                        .fold(0) { acc, loot -> acc + loot.quantity }

                    writeHeader(writer, filename, i)

                    for ((itemId, totalQty) in lootsAggregate.entries.sortedByDescending { it.value }) {
                        val imageUri = (itemsDefinition.itemsById[itemId]
                            ?.element
                            ?.getElementsByTagName("img")
                            ?.item(0) as? Element)
                            ?.getAttribute("uri")
                            ?.takeIf { it.isNotBlank() }

                        // .logs up one level
                        val imagePath = "../assets/game/data/$imageUri"
                        writeItemCard(writer, imagePath, itemId, totalQty)
                    }

                    writer.write(
                        """
                        </div>
                    </div>
                    """.trimIndent()
                    )
                }
            }

            writer.write(
                """
                    </body>
                </html>
                """.trimIndent()
            )
        }

        println("Loot generation logs written to: ${logFile.absolutePath}")
    }

    private fun writeHeader(writer: BufferedWriter, filename: String, iteration: Int) {
        writer.write(
            """
            <div class="scene">
                <div class="scene-header">
                    Scene: $filename
                    | areaLevel: ${PARAMETER1.areaLevel}
                    | iteration: $iteration
                </div>

                <div class="items-grid">
            """.trimIndent()
        )
    }

    private fun writeItemCard(writer: BufferedWriter, imagePath: String, itemId: String, quantity: Int) {
        writer.write(
            """
            <div class="item-card">
                <div class="placeholder-img" style="display:none;">?</div>

                 <img src="$imagePath" alt="$itemId" onerror="
                    this.style.display='none';
                    this.previousElementSibling.style.display='flex';
                 ">

                <div class="item-info">
                    <div class="item-id">$itemId</div>
                    <div class="qty">Total x$quantity</div>
                </div>
            </div>
            """.trimIndent()
        )
    }

    private fun prepareHtmlTemplate(writer: BufferedWriter) {
        writer.write(
            """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>Loot Generation Report</title>

                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background: #e7e7e7;
                        color: #181818;
                        margin: 0;
                        padding: 24px;
                    }

                    h1 {
                        margin-bottom: 8px;
                    }

                    .summary {
                        background: #c2c2c2;
                        padding: 16px;
                        border-radius: 12px;
                        margin-bottom: 24px;
                    }

                    .scene {
                        margin-bottom: 32px;
                        background: #c9c9c9;
                        border-radius: 12px;
                        padding: 16px;
                    }

                    .scene-header {
                        margin-bottom: 16px;
                        font-size: 20px;
                        font-weight: bold;
                        color: #1e1d1a;
                    }

                    .items-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
                        gap: 16px;
                    }

                    .item-card {
                        background: #a6a6a6;
                        border-radius: 10px;
                        padding: 12px;
                        display: flex;
                        gap: 12px;
                        align-items: center;
                        transition: transform 0.15s ease;
                    }

                    .item-card img {
                        width: 64px;
                        height: 64px;
                        object-fit: contain;
                        background: #1a1a1a;
                        border-radius: 8px;
                        padding: 4px;
                    }

                    .item-info {
                        flex: 1;
                    }

                    .item-id {
                        font-weight: bold;
                        margin-bottom: 4px;
                    }

                    .qty {
                        color: #382538;
                    }

                    .header {
                        margin-top: 10px;
                        opacity: 0.7;
                        font-size: 12px;
                    }
                    
                    .placeholder-img {
                        width: 64px;
                        height: 64px;
                        background: #505050;
                        border-radius: 8px;

                        display: flex;
                        align-items: center;
                        justify-content: center;

                        color: #bbb;
                        font-size: 28px;
                        font-weight: bold;

                        flex-shrink: 0;
                    }
                </style>
            </head>
            <body>

            <h1>Loot Generation Report</h1>

            <div class="summary">
                <div><b>Scenes tested:</b> ${SCENES_TO_TEST.size}</div>
                <div><b>Iterations per scene:</b> $GENERATION_PER_SCENE</div>
                <div><b>Area level:</b> ${PARAMETER1.areaLevel}</div>
                <div class="header">
                    Generated at ${LocalDateTime.now()}
                </div>
            </div>

            """.trimIndent()
        )
    }
}
