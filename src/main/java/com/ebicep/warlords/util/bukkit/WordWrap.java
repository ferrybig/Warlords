package com.ebicep.warlords.util.bukkit;

import com.ebicep.warlords.util.chat.DefaultFontInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;

import java.util.ArrayList;
import java.util.List;

public class WordWrap {

    @Deprecated
    public static String wrapWithNewline(String line, int width) {
        if (line.contains("\n")) {
            StringBuilder sb = new StringBuilder();
            for (String s : line.split("\n")) {
                sb.append(wrapWithNewline(s, width)).append("\n");
            }
            sb.setLength(sb.length() - 1);
            return sb.toString();
        }
        StringBuilder output = new StringBuilder(line.length() + 16);
        int lastOffset = 0;
        int previousOffset = 0;
        int currentLength = 0;
        for (int pendingOffset = 0; pendingOffset < line.length(); pendingOffset++) {
            char c = line.charAt(pendingOffset);
            int length;
            if (c == '§' || (pendingOffset - 1 > 0 && line.charAt(pendingOffset - 1) == '§')) {
                length = 0;
            } else {
                length = DefaultFontInfo.getDefaultFontInfo(c).getLength();
            }
            currentLength += length;
            if (Character.isWhitespace(c)) {
                lastOffset = pendingOffset + 1;
            } else if (currentLength > width) {
                if (lastOffset != previousOffset) {
                    output.append(line, previousOffset, lastOffset);
                    output.append('\n');
                    previousOffset = lastOffset;
                    pendingOffset = lastOffset;
                } else {
                    output.append(line, previousOffset, pendingOffset);
                    output.append('\n');
                    previousOffset = pendingOffset;
                    lastOffset = pendingOffset;
                }
                currentLength = 0;
            }

        }
        int pendingOffset = line.length();
        if (previousOffset != pendingOffset) {
            output.append(line, previousOffset, pendingOffset);
            output.append('\n');
        }
        if (output.length() > 0) {
            output.setLength(output.length() - 1);
        }
        return output.toString();
    }

    /**
     * Used for lore, as the client cant display new line characters
     *
     * @param component The component to wrap
     * @param width     The width to wrap at
     * @return The wrapped component
     */
    public static List<Component> wrap(TextComponent component, int width) {
        List<Component> output = new ArrayList<>();
        int currentWidth = 0;

        List<WordInfo> words = new ArrayList<>();
        addChildren(component, words);

        TextComponent.Builder toAppend = Component.text().append(Component.text());
        TextComponent.Builder lastComponent = Component.text();
        Style lastStyle = Style.empty();
        for (int i = 0; i < words.size(); i++) {
            WordInfo wordInfo = words.get(i);
            String word = wordInfo.word();
            Style currentStyle = wordInfo.style();
            if (word.equals("\n")) {
                toAppend.append(lastComponent);
                output.add(toAppend.build());
                toAppend = Component.text();
                lastComponent = Component.text().style(lastStyle);
                currentWidth = 0;
                continue;
            }
            int wordLength = DefaultFontInfo.getStringLength(word);
            String spacer = !word.equals("(") &&
                                    (i < words.size() - 1 &&
                                            !words.get(i + 1).word().equals(".") &&
                                            !words.get(i + 1).word().equals(",") &&
                                            !words.get(i + 1).word().equals(":"))
                            ? " " : ""; //TODO TEMP SOLUTION (fix for period/comma being spaced)
            wordLength += DefaultFontInfo.getStringLength(spacer);
            if (currentWidth + wordLength <= width) {
                if (lastStyle == currentStyle) {
                    lastComponent.content(lastComponent.content() + word + spacer);
                } else {
                    if (!lastComponent.content().isEmpty()) {
                        toAppend.append(lastComponent);
                    }
                    lastComponent = Component.text(word + spacer, currentStyle).toBuilder();
                }
                currentWidth += wordLength;
            } else {
                toAppend.append(lastComponent);
                output.add(toAppend.build());
                toAppend = Component.text();
                lastComponent = Component.text(word + spacer, currentStyle).toBuilder();
                currentWidth = wordLength;
            }
            if (i == words.size() - 1) {
                toAppend.append(lastComponent);
                output.add(toAppend.build());
            }
            lastStyle = currentStyle;
        }
        return output;
    }

    private static void addChildren(Component component, List<WordInfo> words) {
        List<Component> components = new ArrayList<>(component.children());
        if (components.isEmpty()) {
            components.add(0, component.children(new ArrayList<>()));
            for (Component child : components) {
                if (!(child instanceof TextComponent textComponent)) {
                    continue;
                }
                String content = textComponent.content();
                if (content.isEmpty()) {
                    continue;
                }
                content = content.replaceAll("\n", " \n ");
                String[] split = content.split(" ");
                for (String s : split) {
                    if (s.isEmpty()) {
                        continue;
                    }
                    words.add(new WordInfo(s, child.applyFallbackStyle(component.style()).style()));
                }
            }
            return;
        }
        components.add(0, component.children(new ArrayList<>()));

        for (Component child : components) {
            addChildren(child, words);
        }
    }

    /**
     * Do not use for item lore because this adds a new child to one component so if used in lore it will be one long line
     *
     * @param component The component to wrap
     * @param width     The width to wrap at
     * @return The wrapped component
     */
    public static TextComponent wrapWithNewline(TextComponent component, int width) {
        TextComponent.Builder output = Component.text();
        int currentWidth = 0;
        List<Component> components = new ArrayList<>();
        components.add(component);
        components.addAll(component.children());
        for (Component child : components) {
            if (!(child instanceof TextComponent textComponent)) {
                continue;
            }
            String content = textComponent.content();
            if (content.isEmpty()) {
                continue;
            }
            //TODO check blank
            String[] words = content.split(" ");
            StringBuilder toAppend = new StringBuilder();
            for (String word : words) {
                int wordLength = DefaultFontInfo.getStringLength(word);
                if (currentWidth + wordLength <= width) {
                    toAppend.append(word).append(" ");
                    currentWidth += wordLength;
                } else {
                    toAppend.setLength(Math.max(toAppend.length() - 1, 0));
                    output.append(Component.text(toAppend.toString(), child.style()));
                    output.append(Component.newline());
                    toAppend.setLength(0);
                    toAppend.append(word).append(" ");
                    currentWidth = wordLength;
                }
            }
            toAppend.setLength(toAppend.length() - 1);
            output.append(Component.text(toAppend.toString(), child.style()));
        }
        return output.build();
    }

    private record WordInfo(String word, Style style) {
    }

}