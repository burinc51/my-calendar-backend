package com.mycalendar.dev.util;


import com.mycalendar.dev.enums.LikeMode;
import com.mycalendar.dev.exception.InvalidFieldException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TypeSafe {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @SuppressWarnings("unchecked")
    public static <T> T safe(Object obj, Class<T> targetType) {
        if (obj == null || targetType == null) return null;

        Object result = null;

        if (targetType == String.class) {
            result = toString(obj);
        } else if (targetType == Integer.class) {
            result = toInt(obj);
        } else if (targetType == Long.class) {
            result = toLong(obj);
        } else if (targetType == Double.class) {
            result = toDouble(obj);
        } else if (targetType == BigDecimal.class) {
            result = toBigDecimal(obj);
        } else if (targetType == Boolean.class) {
            result = toBoolean(obj);
        } else if (targetType == LocalDate.class) {
            result = toLocalDate(obj);
        } else if (targetType == LocalDateTime.class) {
            result = toLocalDateTime(obj);
        } else if (targetType == Instant.class) {
            result = toInstant(obj);
        }

        return (T) result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T safe(Object obj) {
        if (obj == null) return null;

        return switch (obj) {
            case String s -> (T) s;
            case Integer i -> (T) i;
            case Long l -> (T) l;
            case Double d -> (T) d;
            case BigDecimal bd -> (T) bd;
            case Boolean b -> (T) b;
            case LocalDate ld -> (T) ld;
            case LocalDateTime ldt -> (T) ldt;
            case Instant ins -> (T) ins;
            case Number n -> (T) Double.valueOf(n.doubleValue());
            default -> (T) obj.toString();
        };
    }

    public static String wrapLike(String value, LikeMode mode) {
        if (value == null) return null;
        String safeValue = safe(value);

        return switch (mode) {
            case START -> safeValue + "%";
            case END -> "%" + safeValue;
            case ANYWHERE -> "%" + safeValue + "%";
            case EXACT -> safeValue;
        };
    }

    public static Integer toInt(Object obj) {
        return (obj instanceof BigDecimal bd) ? Integer.valueOf(bd.intValue()) :
                (obj instanceof Number num) ? num.intValue() : null;
    }

    public static Long toLong(Object obj) {
        return (obj instanceof BigDecimal bd) ? Long.valueOf(bd.longValue()) :
                (obj instanceof Number num) ? num.longValue() : null;
    }

    public static Double toDouble(Object obj) {
        return (obj instanceof BigDecimal bd) ? Double.valueOf(bd.doubleValue()) :
                (obj instanceof Number num) ? num.doubleValue() : null;
    }

    public static Boolean toBoolean(Object obj) {
        switch (obj) {
            case null -> {
                return null;
            }
            case Boolean b -> {
                return b;
            }
            case Number num -> {
                return num.intValue() != 0;
            }
            case String s -> {
                String val = s.trim().toLowerCase();
                return val.equals("y") || val.equals("yes")
                        || val.equals("true") || val.equals("1");
            }
            default -> {
            }
        }
        return null;
    }

    public static String toString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String s) return s;
        return obj.toString();
    }

    public static BigDecimal toBigDecimal(Object obj) {
        switch (obj) {
            case null -> {
                return null;
            }
            case BigDecimal bd -> {
                return bd;
            }
            case Number num -> {
                return BigDecimal.valueOf(num.doubleValue());
            }
            default -> {
                try {
                    String str = obj.toString().trim();
                    if (str.matches("[+-]?\\d*(\\.\\d+)?([eE][+-]?\\d+)?")) {
                        return new BigDecimal(str);
                    }
                } catch (NumberFormatException ignored) {
                }
                return null;
            }
        }
    }

    public static LocalDate toLocalDate(Object obj) {
        switch (obj) {
            case null -> {
                return null;
            }
            case LocalDate ld -> {
                return ld;
            }
            case java.sql.Date d -> {
                return d.toLocalDate();
            }
            case Date d -> {
                return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            case Instant ins -> {
                return ins.atZone(ZoneId.systemDefault()).toLocalDate();
            }
            case String s -> {
                try {
                    return LocalDate.parse(s, DATE_FORMAT);
                } catch (Exception e) {
                    try {
                        return LocalDate.parse(s);
                    } catch (Exception ignored) {
                    }
                }
            }
            default -> {
            }
        }
        return null;
    }

    public static LocalDateTime toLocalDateTime(Object obj) {
        switch (obj) {
            case null -> {
                return null;
            }
            case LocalDateTime ldt -> {
                return ldt;
            }
            case java.sql.Timestamp ts -> {
                return ts.toLocalDateTime();
            }
            case Date d -> {
                return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            case Instant ins -> {
                return ins.atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            case String s -> {
                try {
                    return LocalDateTime.parse(s, DATETIME_FORMAT);
                } catch (Exception e) {
                    try {
                        return LocalDateTime.parse(s);
                    } catch (Exception ignored) {
                    }
                }
            }
            default -> {
            }
        }
        return null;
    }

    public static Instant toInstant(Object obj) {
        switch (obj) {
            case null -> {
                return null;
            }
            case Instant ins -> {
                return ins;
            }
            case Date d -> {
                return d.toInstant();
            }
            default -> {
            }
        }
        switch (obj) {
            case LocalDateTime ldt -> {
                return ldt.atZone(ZoneId.systemDefault()).toInstant();
            }
            case LocalDate ld -> {
                return ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
            }
            case String s -> {
                try {
                    return Instant.parse(s);
                } catch (Exception e) {
                    try {
                        return LocalDateTime.parse(s, DATETIME_FORMAT).atZone(ZoneId.systemDefault()).toInstant();
                    } catch (Exception ignored) {
                    }
                }
            }
            default -> {
            }
        }
        return null;
    }

    public static List<Integer> toIntList(Object[] objs) {
        return convertList(objs, TypeSafe::toInt);
    }

    public static List<Long> toLongList(Object[] objs) {
        return convertList(objs, TypeSafe::toLong);
    }

    public static List<Double> toDoubleList(Object[] objs) {
        return convertList(objs, TypeSafe::toDouble);
    }

    public static List<Boolean> toBooleanList(Object[] objs) {
        return convertList(objs, TypeSafe::toBoolean);
    }

    public static List<String> toStringList(Object[] objs) {
        return convertList(objs, TypeSafe::toString);
    }

    public static List<BigDecimal> toBigDecimalList(Object[] objs) {
        return convertList(objs, TypeSafe::toBigDecimal);
    }

    public static List<LocalDate> toLocalDateList(Object[] objs) {
        return convertList(objs, TypeSafe::toLocalDate);
    }

    public static List<LocalDateTime> toLocalDateTimeList(Object[] objs) {
        return convertList(objs, TypeSafe::toLocalDateTime);
    }

    public static List<Instant> toInstantList(Object[] objs) {
        return convertList(objs, TypeSafe::toInstant);
    }

    public static <T> List<T> toList(Collection<?> objs, Function<Object, T> converter) {
        if (objs == null) return List.of();
        return objs.stream()
                .map(converter)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static <T> List<T> convertList(Object[] objs, Function<Object, T> converter) {
        if (objs == null) return List.of();
        return Arrays.stream(objs)
                .map(converter)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static <T> List<T> safeList(Collection<?> objs, Class<T> targetType) {
        if (objs == null) return List.of();
        return objs.stream()
                .map(o -> safe(o, targetType))
                .filter(Objects::nonNull)
                .toList();
    }

    public static <T> List<T> safeList(Object[] objs, Class<T> targetType) {
        if (objs == null) return List.of();
        return Arrays.stream(objs)
                .map(o -> safe(o, targetType))
                .filter(Objects::nonNull)
                .toList();
    }

    public static void validateSortBy(String sortBy, Class<?> entityClass) {
        if (sortBy == null) return;

        List<String> validFields = new ArrayList<>();

        Class<?> current = entityClass;
        while (current != null && current != Object.class) {
            validFields.addAll(
                    Arrays.stream(current.getDeclaredFields())
                            .map(Field::getName)
                            .toList()
            );
            current = current.getSuperclass();
        }

        if (!validFields.contains(sortBy)) {
            throw new InvalidFieldException(sortBy, validFields);
        }
    }

}