package com.shopease.checkout.notification;

import com.shopease.checkout.common.model.NotificationChannel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * FACTORY PATTERN: Returns the correct NotificationSender for a given channel.
 */
@Component
public class NotificationSenderFactory {

    private final Map<NotificationChannel, NotificationSender> senders;

    public NotificationSenderFactory(List<NotificationSender> senderList) {
        this.senders = senderList.stream()
                .collect(Collectors.toMap(NotificationSender::getChannel, Function.identity()));
    }

    public NotificationSender create(NotificationChannel channel) {
        NotificationSender sender = senders.get(channel);
        if (sender == null) {
            throw new IllegalArgumentException(
                    "No sender registered for channel: " + channel
            );
        }
        return sender;
    }

    public NotificationSender getEmailFallback() {
        return create(NotificationChannel.EMAIL);
    }
}
