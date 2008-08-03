/*
 * Copyright (c) 2008, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javax.time.calendar;

import static org.testng.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.time.Instant;
import javax.time.calendar.TimeZone.Discontinuity;
import javax.time.calendar.TimeZone.OffsetInfo;

import org.testng.annotations.Test;

/**
 * Test TimeZone.
 *
 * @author Stephen Colebourne
 */
@Test
public class TestTimeZone {

    //-----------------------------------------------------------------------
    // Basics
    //-----------------------------------------------------------------------
    public void test_interfaces() {
        assertTrue(TimeZone.UTC instanceof Serializable);
    }

    public void test_immutable() {
        Class<ZoneOffset> cls = ZoneOffset.class;
        assertTrue(Modifier.isPublic(cls.getModifiers()));
        assertTrue(Modifier.isFinal(cls.getModifiers()));
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) == false) {
                assertTrue(Modifier.isPrivate(field.getModifiers()));
                assertTrue(Modifier.isFinal(field.getModifiers()));
            }
        }
    }

    public void test_serialization_fixed() throws Exception {
        TimeZone test = TimeZone.timeZone("UTC+01:30");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(test);
        baos.close();
        byte[] bytes = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bais);
        TimeZone result = (TimeZone) in.readObject();
        
        assertSame(result, test);
    }

    public void test_serialization_dynamic() throws Exception {
        TimeZone test = TimeZone.timeZone("Europe/London");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(test);
        baos.close();
        byte[] bytes = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bais);
        TimeZone result = (TimeZone) in.readObject();
        
        assertSame(result, test);
    }

    //-----------------------------------------------------------------------
    // UTC
    //-----------------------------------------------------------------------
    public void test_constant_UTC() {
        TimeZone test = TimeZone.UTC;
        assertEquals(test.getID(), "UTC");
        assertEquals(test.isFixed(), true);
        assertEquals(test.getOffset(Instant.instant(0L)), ZoneOffset.UTC);
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 6, 30)), ZoneOffset.UTC);
        assertSame(test, TimeZone.timeZone("UTC"));
        assertSame(test, TimeZone.timeZone(ZoneOffset.UTC));
    }

    //-----------------------------------------------------------------------
    public void test_factory_string_UTC() {
        String[] values = new String[] {
            "Z",
            "+00","+0000","+00:00","+000000","+00:00:00",
            "-00","-0000","-00:00","-000000","-00:00:00",
        };
        for (int i = 0; i < values.length; i++) {
            TimeZone test = TimeZone.timeZone("UTC" + values[i]);
            assertSame(test, TimeZone.UTC);
        }
    }

    public void test_factory_string_invalid() {
        String[] values = new String[] {
            "A","B","C","D","E","F","G","H","I","J","K","L","M",
            "N","O","P","Q","R","S","T","U","V","W","X","Y","ZZ",
            "+0","+0:00","+00:0","+0:0",
            "+000","+00000",
            "+0:00:00","+00:0:00","+00:00:0","+0:0:0","+0:0:00","+00:0:0","+0:00:0",
            "+01_00","+01;00","+01@00","+01:AA",
            "+19","+19:00","+18:01","+18:00:01","+1801","+180001",
            "-0","-0:00","-00:0","-0:0",
            "-000","-00000",
            "-0:00:00","-00:0:00","-00:00:0","-0:0:0","-0:0:00","-00:0:0","-0:00:0",
            "-19","-19:00","-18:01","-18:00:01","-1801","-180001",
            "-01_00","-01;00","-01@00","-01:AA",
            "@01:00",
        };
        for (int i = 0; i < values.length; i++) {
            try {
                TimeZone.timeZone("UTC" + values[i]);
                fail("Should have failed:" + values[i]);
            } catch (IllegalArgumentException ex) {
                // expected
            }
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_factory_string_null() {
        TimeZone.timeZone((String) null);
    }

    //-----------------------------------------------------------------------
    public void test_factory_string_London() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        assertEquals(test.getID(), "Europe/London");
        assertSame(TimeZone.timeZone("Europe/London"), test);
    }

    //-----------------------------------------------------------------------
    // Europe/London
    //-----------------------------------------------------------------------
    public void test_London() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        assertEquals(test.getID(), "Europe/London");
        assertEquals(test.isFixed(), false);
    }

    public void test_London_getOffset() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 1, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 2, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 4, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 5, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 6, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 7, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 8, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 9, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 11, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 12, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 24, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 25, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 26, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 27, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 28, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 29, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 30, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 31, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        
        assertEquals(test.getOffset(OffsetDateTime.dateTime(2008, 3, 30, 0, 0, 0, 999999999, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateTime(2008, 3, 30, 1, 0, 0, 0, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 24, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 25, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 26, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 27, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 28, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 29, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 30, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 31, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        
        assertEquals(test.getOffset(OffsetDateTime.dateTime(2008, 10, 26, 0, 0, 0, 999999999, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateTime(2008, 10, 26, 1, 0, 0, 0, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
    }

    public void test_London_getOffsetInfo() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 1, 1)), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 2, 1)), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 1)), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 4, 1)), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 5, 1)), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 6, 1)), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 7, 1)), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 8, 1)), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 9, 1)), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 1)), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 11, 1)), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 12, 1)), ZoneOffset.zoneOffset(0));
        
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 24)), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 25)), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 26)), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 27)), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 28)), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 29)), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 30)), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 31)), ZoneOffset.zoneOffset(1));
        
        assertEquals(test.getOffsetInfo(LocalDateTime.dateTime(2008, 3, 30, 0, 0, 0, 999999999)), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateTime(2008, 3, 30, 2, 0, 0, 0)), ZoneOffset.zoneOffset(1));
        
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 24)), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 25)), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 26)), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 27)), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 28)), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 29)), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 30)), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 31)), ZoneOffset.zoneOffset(0));
        
        assertEquals(test.getOffsetInfo(LocalDateTime.dateTime(2008, 10, 26, 0, 0, 0, 999999999)), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffsetInfo(LocalDateTime.dateTime(2008, 10, 26, 2, 0, 0, 0)), ZoneOffset.zoneOffset(0));
    }

    public void test_London_getOffsetInfo_gap() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        OffsetInfo info = test.getOffsetInfo(LocalDateTime.dateTime(2008, 3, 30, 1, 0, 0, 0));
        assertEquals(info instanceof Discontinuity, true);
        Discontinuity dis = (Discontinuity) info;
        assertEquals(dis.isGap(), true);
        assertEquals(dis.isOverlap(), false);
        assertEquals(dis.getOffsetBefore(), ZoneOffset.zoneOffset(0));
        assertEquals(dis.getOffsetAfter(), ZoneOffset.zoneOffset(1));
        assertEquals(dis.getTransition(), OffsetDateTime.dateTime(2008, 3, 30, 1, 0, ZoneOffset.UTC).toInstant());
        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-1)), false);
        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(0)), true);
        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(1)), true);
        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(2)), false);
        assertEquals(dis.toString(), "Discontinuity from Z to +01:00");
    }

    public void test_London_getOffsetInfo_overlap() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        OffsetInfo info = test.getOffsetInfo(LocalDateTime.dateTime(2008, 10, 26, 1, 0, 0, 0));
        assertEquals(info instanceof Discontinuity, true);
        Discontinuity dis = (Discontinuity) info;
        assertEquals(dis.isGap(), false);
        assertEquals(dis.isOverlap(), true);
        assertEquals(dis.getOffsetBefore(), ZoneOffset.zoneOffset(1));
        assertEquals(dis.getOffsetAfter(), ZoneOffset.zoneOffset(0));
        assertEquals(dis.getTransition(), OffsetDateTime.dateTime(2008, 10, 26, 1, 0, ZoneOffset.UTC).toInstant());
        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-1)), false);
        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(0)), true);
        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(1)), true);
        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(2)), false);
        assertEquals(dis.toString(), "Discontinuity from +01:00 to Z");
    }

//    //-----------------------------------------------------------------------
//    // toTimeZone()
//    //-----------------------------------------------------------------------
//    public void test_toTimeZone() {
//        TimeZone offset = TimeZone.timeZone(1, 2, 3);
//        assertEquals(offset.toTimeZone(), TimeZone.timeZone(offset));
//    }

//    //-----------------------------------------------------------------------
//    // compareTo()
//    //-----------------------------------------------------------------------
//    public void test_compareTo() {
//        TimeZone offset1 = TimeZone.timeZone(1, 2, 3);
//        TimeZone offset2 = TimeZone.timeZone(2, 3, 4);
//        assertTrue(offset1.compareTo(offset2) > 0);
//        assertTrue(offset2.compareTo(offset1) < 0);
//        assertTrue(offset1.compareTo(offset1) == 0);
//        assertTrue(offset2.compareTo(offset2) == 0);
//    }

    //-----------------------------------------------------------------------
    // equals() / hashCode()
    //-----------------------------------------------------------------------
    public void test_equals() {
        TimeZone test1 = TimeZone.timeZone("Europe/London");
        TimeZone test2 = TimeZone.timeZone("Europe/Paris");
        TimeZone test2b = TimeZone.timeZone("Europe/Paris");
        assertEquals(test1.equals(test2), false);
        assertEquals(test2.equals(test1), false);
        
        assertEquals(test1.equals(test1), true);
        assertEquals(test2.equals(test2), true);
        assertEquals(test2.equals(test2b), true);
        
        assertEquals(test1.hashCode() == test1.hashCode(), true);
        assertEquals(test2.hashCode() == test2.hashCode(), true);
        assertEquals(test2.hashCode() == test2b.hashCode(), true);
    }

    public void test_equals_null() {
        assertEquals(TimeZone.timeZone("Europe/London").equals(null), false);
    }

    public void test_equals_notTimeZone() {
        assertEquals(TimeZone.timeZone("Europe/London").equals("Europe/London"), false);
    }

    //-----------------------------------------------------------------------
    // toString()
    //-----------------------------------------------------------------------
    public void test_toString() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        assertEquals(test.toString(), "Europe/London");
        test = TimeZone.timeZone("UTC+01:02:03");
        assertEquals(test.toString(), "UTC+01:02:03");
        test = TimeZone.UTC;
        assertEquals(test.toString(), "UTC");
    }

}