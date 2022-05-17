package love.marblegate.evilgemira

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import love.marblegate.evilgemira.EvilService.saveTo
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.message.data.*
import java.util.*

object GemiraService {
    private val datas = listOf(mutableMapOf(),mutableMapOf<IntArray, MessageChain>())
    private var activeMap = 0

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
                builder.append(At(event.authorId)).append(" 刚刚撤回了:\n").append(messageChain)
                CoroutineScope(Dispatchers.Default).launch{
                    event.group.sendMessage(builder.asMessageChain())
                }
            }
        }
    }

    fun saveMessageTOData(event: GroupMessageEvent){
        if(event.message.valid())
            datas[activeMap][event.message.ids] = event.message
    }

    private fun MessageRecallEvent.GroupRecall.valid(): Boolean{
        val a = this.operator?.permission?.ordinal
        if (a != null) {
            if(a==0) return true
        }
        return false
    }

    private fun MessageRecallEvent.GroupRecall.retrieve(): MessageChain?{
        if(datas[0].contains(this.messageIds)){
            return datas[0][this.messageIds]
        } else {
            if(datas[1].contains(this.messageIds))
                return datas[1][this.messageIds]
        }
        return null
    }

    private fun MessageChain.valid(): Boolean {
        if (this.contains(QuoteReply)) {
            return false
        }
        for (message in this) {
            if (!(message is MessageSource || message is PlainText || message is At || message is Image)) return false
        }
        return true
    }

    private fun save(message: MessageChain) {
        message.saveTo("TODO")
        // TODO use EvilService shit to save content to local
    }
}