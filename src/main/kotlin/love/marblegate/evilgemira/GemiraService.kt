package love.marblegate.evilgemira

import com.google.common.collect.HashBasedTable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import love.marblegate.evilgemira.EvilService.saveTo
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.message.data.*
import java.io.File
import java.util.*

object GemiraService {
    private val datas = listOf(HashBasedTable.create<Long, Int, MessageChain>(),HashBasedTable.create())
    private var activeMap = 0
    private val path = System.getProperty("user.dir") + File.separator + "data" + File.separator + "evil_gemira" + File.separator

    init {
        Timer().schedule(object : TimerTask(){
            override fun run() {
                val a = if (activeMap==0) 1 else 0
                datas[a].clear()
                activeMap = a

            }
        }, 1000 * 2 * 60, 1000 * 2 * 60)
    }

    fun handle(event: MessageRecallEvent.GroupRecall) {
        if(event.valid()){
            val messageChain = event.retrieve()
            if(messageChain!=null){
                val builder = MessageChainBuilder()
                builder.append(At(event.authorId))
                if(messageChain.contains(QuoteReply)){
                    builder.append(" 刚刚撤回一条引用消息，内容如下：\n")
                } else {
                    builder.append(" 刚刚撤回一条消息，内容如下：\n")
                }
                builder.append(messageChain.filter { singleMessage -> singleMessage !is MessageMetadata }.toMessageChain())
                CoroutineScope(Dispatchers.IO).launch{
                    event.group.sendMessage(builder.asMessageChain())
                    // Pandora's box
                    // EvilService.DataUnit(event,messageChain).saveTo(path)
                }
            }
        }
    }

    fun saveMessageTOData(event: GroupMessageEvent){
        if(event.message.valid())
            datas[activeMap].put(event.group.id,event.time,event.message)
    }

    private fun MessageRecallEvent.GroupRecall.valid(): Boolean{
        val a = this.operator?.permission?.ordinal
        if (a != null) {
            if(a==0) return true
        }
        return false
    }

    private fun MessageRecallEvent.GroupRecall.retrieve(): MessageChain?{
        if(datas[0].contains(this.group.id,this.messageTime)){
            return datas[0].get(this.group.id,this.messageTime)
        } else {
            if(datas[1].contains(this.group.id,this.messageTime))
                return datas[1][this.group.id,this.messageTime]
        }
        return null
    }

    private fun MessageChain.valid(): Boolean {
        for (message in this) {
            if (!(message is QuoteReply || message is MessageSource || message is PlainText || message is At || message is Image)) return false
        }
        return true
    }
}