package jp.xhw.datapackupload.plugin.discord;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class MessageHandler {

    final protected DiscordBot bot;

    protected MessageHandler(DiscordBot bot) {
        this.bot = bot;
    }

    public abstract void handle(MessageReceivedEvent event);

}
