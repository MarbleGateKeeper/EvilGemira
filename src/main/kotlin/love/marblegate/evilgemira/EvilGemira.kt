package love.marblegate.evilgemira

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.utils.info

object EvilGemira : KotlinPlugin(
    JvmPluginDescription(
        id = "love.marblegate.evilgemira",
        name = "evilgemira",
        version = "0.1.0",
    ) {
        author("MarbleGate")
    }
) {
    override fun onEnable() {
        logger.info { "EvilGemira Plugin loaded" }
        GlobalEventChannel.subscribeAlways<GroupMessageEvent>{ event -> GemiraService.saveMessageTOData(event) }
        GlobalEventChannel.subscribeAlways<MessageRecallEvent.GroupRecall>{ event -> GemiraService.handle(event) }
    }
}