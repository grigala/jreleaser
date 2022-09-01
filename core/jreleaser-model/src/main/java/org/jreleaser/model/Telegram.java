/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.model;

import org.jreleaser.bundle.RB;
import org.jreleaser.util.Env;
import org.jreleaser.util.JReleaserException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;

import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.KEY_TAG_NAME;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public class Telegram extends AbstractAnnouncer<Telegram> {
    public static final String NAME = "telegram";
    public static final String TELEGRAM_TOKEN = "TELEGRAM_TOKEN";
    public static final String TELEGRAM_CHAT_ID = "TELEGRAM_CHAT_ID";

    private String token;
    private String chatId;
    private String message;
    private String messageTemplate;

    public Telegram() {
        super(NAME);
    }

    @Override
    public void merge(Telegram telegram) {
        freezeCheck();
        super.merge(telegram);
        this.token = merge(this.token, telegram.token);
        this.chatId = merge(this.chatId, telegram.chatId);
        this.message = merge(this.message, telegram.message);
        this.messageTemplate = merge(this.messageTemplate, telegram.messageTemplate);
    }

    public String getResolvedMessage(JReleaserContext context) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        return resolveTemplate(message, props);
    }

    public String getResolvedMessageTemplate(JReleaserContext context, Map<String, Object> extraProps) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(KEY_TAG_NAME, context.getModel().getRelease().getGitService()
            .getEffectiveTagName(context.getModel()));
        props.putAll(extraProps);

        Path templatePath = context.getBasedir().resolve(messageTemplate);
        try {
            Reader reader = java.nio.file.Files.newBufferedReader(templatePath);
            return applyTemplate(reader, props);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_reading_template",
                context.relativizeToBasedir(templatePath)));
        }
    }

    public String getResolvedToken() {
        return Env.env(TELEGRAM_TOKEN, token);
    }

    public String getResolvedChatId() {
        return Env.env(TELEGRAM_CHAT_ID, chatId);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        freezeCheck();
        this.token = token;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        freezeCheck();
        this.chatId = chatId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        freezeCheck();
        this.message = message;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        freezeCheck();
        this.messageTemplate = messageTemplate;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("token", isNotBlank(getResolvedToken()) ? HIDE : UNSET);
        props.put("chatId", isNotBlank(getResolvedChatId()) ? HIDE : UNSET);
        props.put("message", message);
        props.put("messageTemplate", messageTemplate);
    }
}
