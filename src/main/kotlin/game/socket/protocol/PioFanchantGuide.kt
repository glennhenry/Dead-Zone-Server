package game.socket.protocol

import encore.network.fanchant.Fanchant
import encore.network.fanchant.guide.DecodeResult
import encore.network.fanchant.guide.FanchantGuide
import encore.utils.startsWithBytes

class PioFanchantGuide: FanchantGuide<List<Any>> {
    override fun verify(data: ByteArray): Boolean {
        if (data.isEmpty()) return false
        if (data.startsWithBytes(byteArrayOf(0x00))) return false
        return true
    }

    override fun tryDecode(data: ByteArray): DecodeResult<List<Any>> {
        return DecodeResult.Success(PIODeserializer.deserialize(data))
    }

    override fun materialize(decoded: List<Any>): Fanchant {
        return PioFanchant.fromRaw(decoded)
    }
}
