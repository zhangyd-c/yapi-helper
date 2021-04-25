package com.xkcoding.idea.plugins.yapi_helper.util;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.xkcoding.idea.plugins.yapi_helper.icons.SdkIcons;

public class NotificationUtil {

    public static NotificationGroup notificationGroup = new NotificationGroup("Java2Json.NotificationGroup",
            NotificationDisplayType.BALLOON, true, null, SdkIcons.Logo);

    public static void warnNotify(String message, Project project) {
        Notifications.Bus.notify(notificationGroup.createNotification(message, NotificationType.WARNING), project);
    }

    public static void infoNotify(String message, Project project) {
        Notifications.Bus.notify(notificationGroup.createNotification(message, NotificationType.INFORMATION), project);
    }

    public static void infoNotify(String title, String message, Project project) {
        Notifications.Bus.notify(notificationGroup.createNotification(title, null, message, NotificationType.INFORMATION), project);
    }

    public static void infoNotify(String title, String subTitle, String message, Project project) {
        Notifications.Bus.notify(notificationGroup.createNotification(title, subTitle, message, NotificationType.INFORMATION), project);
    }

    public static void errorNotify(String title, String message, Project project) {
        Notifications.Bus.notify(notificationGroup.createNotification(title, null, message, NotificationType.ERROR), project);
    }

    public static void errorNotify(String message, Project project) {
        Notifications.Bus.notify(notificationGroup.createNotification("YApi Helper", null, message, NotificationType.ERROR), project);
    }

    public static void errorNotify(String title, String message) {
        Notifications.Bus.notify(notificationGroup.createNotification(title, null, message, NotificationType.ERROR));
    }

    public static void errorNotify(String message) {
        Notifications.Bus.notify(notificationGroup.createNotification("YApi Helper", null, message, NotificationType.ERROR));
    }

}
