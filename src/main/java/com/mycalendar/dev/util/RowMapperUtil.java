package com.mycalendar.dev.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class RowMapperUtil {

    private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd MM yyyy")
    };
    private static final DateTimeFormatter[] DATETIME_FORMATS = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
    };

    private static final DateTimeFormatter OUTPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Retrieves a value from the given row at the specified index and converts it to the target type.
     *
     * @param row        The row of data, typically an Object array.
     * @param index      The index of the value to retrieve.
     * @param targetType The class type to which the value should be converted.
     * @param <T>        The type parameter for the target type.
     * @return The value at the specified index converted to the target type, or null if not found or conversion fails.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(Object[] row, int index, Class<T> targetType) {
        if (row == null || index < 0 || index >= row.length) return null;

        Object val = row[index];
        if (val == null) return null;

        try {
            if (targetType == String.class) {
                String str = tryFormatDate(val);
                if (str != null) return (T) str;
                return (T) String.valueOf(val);
            }

            if (targetType.isInstance(val)) return (T) val;
            if (targetType == Long.class) return (T) toLong(val);
            if (targetType == Integer.class) return (T) toInteger(val);
            if (targetType == Double.class) return (T) toDouble(val);
            if (targetType == BigDecimal.class) return (T) toBigDecimal(val);
            if (targetType == Boolean.class) return (T) toBoolean(val);
            if (targetType == LocalDate.class) return (T) toLocalDate(val);
            if (targetType == LocalDateTime.class) return (T) toLocalDateTime(val);

            return null; // ไม่รู้จัก type → คืน null
        } catch (Exception e) {
            return null; // ถ้าเจอ error ตอน cast/parse → คืน null
        }
    }

    private static String tryFormatDate(Object v) {
        try {
            LocalDate date = null;
            if (v instanceof LocalDate ld) date = ld;
            else if (v instanceof LocalDateTime ldt) date = ldt.toLocalDate();
            else if (v instanceof Timestamp ts) date = ts.toLocalDateTime().toLocalDate();
            else if (v instanceof Date d) date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            else if (v instanceof CharSequence cs) {
                String s = cs.toString().trim();
                if (s.isEmpty()) return null;

                // try parse as LocalDate
                for (DateTimeFormatter fmt : DATE_FORMATS) {
                    try {
                        return LocalDate.parse(s, fmt).format(OUTPUT_DATE_FORMAT);
                    } catch (Exception ignored) {
                    }
                }
                // try parse as LocalDateTime
                for (DateTimeFormatter fmt : DATETIME_FORMATS) {
                    try {
                        return LocalDateTime.parse(s, fmt).toLocalDate().format(OUTPUT_DATE_FORMAT);
                    } catch (Exception ignored) {
                    }
                }
                try {
                    return OffsetDateTime.parse(s).toLocalDate().format(OUTPUT_DATE_FORMAT);
                } catch (Exception ignored) {
                }
                try {
                    return ZonedDateTime.parse(s).toLocalDate().format(OUTPUT_DATE_FORMAT);
                } catch (Exception ignored) {
                }
            }
            return date != null ? date.format(OUTPUT_DATE_FORMAT) : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Long toLong(Object v) {
        try {
            if (v instanceof Long l) return l;
            if (v instanceof Number n) return n.longValue();
            if (v instanceof BigInteger bi) return bi.longValue();
            if (v instanceof String s) return s.isBlank() ? null : Long.valueOf(s.trim());
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Integer toInteger(Object v) {
        try {
            if (v instanceof Integer i) return i;
            if (v instanceof Number n) return n.intValue();
            if (v instanceof BigInteger bi) return bi.intValue();
            if (v instanceof String s) return s.isBlank() ? null : Integer.valueOf(s.trim());
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Double toDouble(Object v) {
        try {
            if (v instanceof Double d) return d;
            if (v instanceof Number n) return n.doubleValue();
            if (v instanceof String s) return s.isBlank() ? null : Double.valueOf(s.trim());
        } catch (Exception ignored) {
        }
        return null;
    }

    private static BigDecimal toBigDecimal(Object v) {
        try {
            if (v instanceof BigDecimal bd) return bd;
            if (v instanceof BigInteger bi) return new BigDecimal(bi);
            if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
            if (v instanceof String s) return s.isBlank() ? null : new BigDecimal(s.trim());
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Boolean toBoolean(Object v) {
        if (v == null) return null;
        try {
            if (v instanceof Boolean b) return b;
            String s = v.toString().trim().toLowerCase();
            if (s.isEmpty()) return null;
            return s.equals("y") || s.equals("yes") || s.equals("true") || s.equals("1");
        } catch (Exception ignored) {
        }
        return null;
    }

    private static LocalDate toLocalDate(Object v) {
        try {
            if (v instanceof LocalDate ld) return ld;
            if (v instanceof java.sql.Date d) return d.toLocalDate();
            if (v instanceof Date d) return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (v instanceof CharSequence cs) {
                String s = cs.toString().trim();
                if (s.isEmpty()) return null;
                for (DateTimeFormatter fmt : DATE_FORMATS) {
                    try {
                        return LocalDate.parse(s, fmt);
                    } catch (Exception ignored) {
                    }
                }
                for (DateTimeFormatter fmt : DATETIME_FORMATS) {
                    try {
                        return LocalDateTime.parse(s, fmt).toLocalDate();
                    } catch (Exception ignored) {
                    }
                }
                try {
                    return OffsetDateTime.parse(s).toLocalDate();
                } catch (Exception ignored) {
                }
                try {
                    return ZonedDateTime.parse(s).toLocalDate();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static LocalDateTime toLocalDateTime(Object v) {
        try {
            if (v instanceof LocalDateTime ldt) return ldt;
            if (v instanceof Timestamp ts) return ts.toLocalDateTime();
            if (v instanceof Date d) return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            if (v instanceof LocalDate ld) return ld.atStartOfDay();
            if (v instanceof CharSequence cs) {
                String s = cs.toString().trim();
                if (s.isEmpty()) return null;
                for (DateTimeFormatter fmt : DATETIME_FORMATS) {
                    try {
                        return LocalDateTime.parse(s, fmt);
                    } catch (Exception ignored) {
                    }
                }
                try {
                    return OffsetDateTime.parse(s).toLocalDateTime();
                } catch (Exception ignored) {
                }
                try {
                    return ZonedDateTime.parse(s).toLocalDateTime();
                } catch (Exception ignored) {
                }
                for (DateTimeFormatter fmt : DATE_FORMATS) {
                    try {
                        return LocalDate.parse(s, fmt).atStartOfDay();
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
