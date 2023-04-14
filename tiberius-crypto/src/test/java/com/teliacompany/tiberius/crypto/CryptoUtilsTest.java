package com.teliacompany.tiberius.crypto;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class CryptoUtilsTest {
    @Test
    public void nameWithNoIv() {
        String encryption1 = CryptoUtils.encrypt("Hej", "superSecret");
        String encryption2 = CryptoUtils.encrypt("Hej", "superSecret");

        //Assert.assertEquals(encryption1, encryption2);

        String decrypted1 = CryptoUtils.decrypt(encryption1, "superSecret");
        String decrypted2 = CryptoUtils.decrypt(encryption2, "superSecret");

        Assert.assertEquals("Hej", decrypted1);
        Assert.assertEquals(decrypted1, decrypted2);
    }

    @Ignore
    @Test
    public void testNeverTheSame() {
        List<String> encodedList = new ArrayList<>();
        int max = 10; //Set me to a high number (tested with 10k max)
        IntStream.rangeClosed(1, max).forEach(i -> {
            String encryption = CryptoUtils.encrypt("test2", "testttt");
            Assert.assertFalse(encodedList.contains(encryption));
            encodedList.add(encryption);
            if(i % 15 == 0) {
                System.out.println(i + "/" + max);
            }
        });

    }
}
