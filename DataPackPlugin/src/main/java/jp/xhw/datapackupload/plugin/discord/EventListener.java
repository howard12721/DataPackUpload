package jp.xhw.datapackupload.plugin.discord;

import jp.xhw.datapackupload.plugin.discord.handlers.*;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class EventListener extends ListenerAdapter {


    private final DiscordBot bot;
    private final MessageHandler messageHandler;
    private final Map<String, ButtonHandler> buttonHandlerMap;

    public EventListener(DiscordBot bot) {
        this.bot = bot;
        this.messageHandler = new MessageHandlerImpl(this.bot);
        this.buttonHandlerMap = Map.of(
                "cancel", new CancelButton(this.bot),
                "enable", new EnableButton(this.bot),
                "overwrite", new OverwriteButton(this.bot),
                "no", new PassEnableButton(this.bot)
        );
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        messageHandler.handle(event);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        final String componentId = event.getComponentId();
        Optional.ofNullable(buttonHandlerMap.get(componentId.substring(0, componentId.indexOf('-')))).ifPresent(button -> button.handle(event));
    }

}
