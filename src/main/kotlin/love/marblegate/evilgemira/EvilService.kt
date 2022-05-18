package love.marblegate.evilgemira

import cn.hutool.http.HttpUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.MiraiInternalApi
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

object EvilService{
    @OptIn(MiraiInternalApi::class)
    fun DataUnit.saveTo(filePath: String){
        val m = if(this.message.contains(QuoteReply)) "引用消息" else "消息"
        val content = this.message.filter { singleMessage -> singleMessage !is MessageMetadata }
        val images = content.filterIsInstance<Image>()
        if(images.isNotEmpty()){
            CoroutineScope(Dispatchers.IO).launch{
                for (image in images) {
                    image.saveTo( "$filePath${this@saveTo.event.group.id}" + File.separator + "image" + File.separator + "${image.imageId}.${image.imageType.formatName}")
                }
            }
        }
        val detailedPath = "$filePath${this.event.group.id}" + File.separator + "${LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)}.txt"
        val ctx = """
            群成员${this.event.author.nick}(${this.event.author.id}) 于 ${LocalDateTime.now()} 撤回了一条$m：
            ${
            content.joinToString("\n") { singleMessage ->
                when (singleMessage) {
                    is At -> "At@${singleMessage.target}"
                    is Image -> "图片内容(已保存至/image/${singleMessage.imageId}.${singleMessage.imageType.formatName})"
                    else -> singleMessage.content
                }
            }}
        """.trimIndent() + "\n"
        CoroutineScope(Dispatchers.IO).launch{
            ctx.appendToRecord(ensureDir(detailedPath))
        }
}

    private fun String.appendToRecord(path: String) {
        OutputStreamWriter(
            Files.newOutputStream(Paths.get(path), StandardOpenOption.APPEND),
            StandardCharsets.UTF_8
        ).use { writer -> writer.write(this) }
    }

    @OptIn(MiraiInternalApi::class)
    private suspend fun Image.saveTo(path:String){
        val stream = ByteArrayInputStream(HttpUtil.downloadBytes(this.queryUrl()))
        val image = withContext(Dispatchers.IO) {
            ImageIO.read(stream)
        }
        withContext(Dispatchers.IO) {
            ImageIO.write(image, this@saveTo.imageType.formatName, File(ensureDir(path)))
        }
    }

    private fun ensureDir(path: String): String {
        val file = File(path)
        val fileParent = file.parentFile
        if (!fileParent.exists()) {
            fileParent.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }
        return path
    }

    data class DataUnit(val event: MessageRecallEvent.GroupRecall, val message: MessageChain);
}
