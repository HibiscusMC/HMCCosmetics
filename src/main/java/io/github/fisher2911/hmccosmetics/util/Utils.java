package io.github.fisher2911.hmccosmetics.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class Utils {

    /**
     * @param original    Object to be checked if null
     * @param replacement Object returned if original is null
     * @return original if not null, otherwise replacement
     */

    public static <T> T replaceIfNull(final @Nullable T original, final @NotNull T replacement) {
        return replaceIfNull(original, replacement, t -> {});
    }

    /**
     *
     * @param original    Object to be checked if null
     * @param replacement Object returned if original is null
     * @param consumer accepts the original object, can be used for logging
     * @return original if not null, otherwise replacement
     */

    public static <T> T replaceIfNull(final @Nullable T original, final T replacement, final @NotNull Consumer<T> consumer)  {
        if (original == null) {
            consumer.accept(replacement);
            return replacement;
        }
        consumer.accept(original);
        return original;
    }

    /**
     *
     * @param t object being checked
     * @param consumer accepted if t is not null
     * @param <T> type
     */

    public static <T> void doIfNotNull(final @Nullable T t, final @NotNull Consumer<T> consumer) {
        if (t == null) {
            return;
        }
        consumer.accept(t);
    }

    /**
     *
     * @param t object being checked
     * @param function applied if t is not null
     * @param <T> type
     * @return
     */

    public static <T> Optional<T> returnIfNotNull(final @Nullable T t, final @NotNull Function<T, T> function) {
        if (t == null) {
            return Optional.empty();
        }
        return Optional.of(function.apply(t));
    }

    /**
     *
     * @param enumAsString Enum value as a string to be parsed
     * @param enumClass enum type enumAsString is to be converted to
     * @param defaultEnum default value to be returned
     * @return enumAsString as an enum, or default enum if it could not be parsed
     */

    public static <E extends Enum<E>> E stringToEnum(final @NotNull String enumAsString,
                                                     final @NotNull Class<E> enumClass,
                                                     E defaultEnum) {
        return stringToEnum(enumAsString, enumClass, defaultEnum, e -> {});
    }

    /**
     *
     * @param enumAsString Enum value as a string to be parsed
     * @param enumClass enum type enumAsString is to be converted to
     * @param defaultEnum default value to be returned
     * @param consumer accepts the returned enum, can be used for logging
     * @return enumAsString as an enum, or default enum if it could not be parsed
     */

    public static <E extends Enum<E>> E stringToEnum(final @NotNull String enumAsString,
                                                              @NotNull final Class<E> enumClass,
                                                              final E defaultEnum,
                                                     final @NotNull Consumer<E> consumer) {
        try {
            final E value = Enum.valueOf(enumClass, enumAsString);
            consumer.accept(value);
            return value;
        } catch (final IllegalArgumentException exception) {
            consumer.accept(defaultEnum);
            return defaultEnum;
        }
    }

}
