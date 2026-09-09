package br.gov.inmetro.beacon.generator;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class BeaconDeviceRandomGeneratorTest {

    @Test
    public void testParseSizeGigabytes() {
        Assert.assertEquals(4L * 1024L * 1024L * 1024L, BeaconDeviceRandomGenerator.parseSize("4G"));
        Assert.assertEquals(4L * 1024L * 1024L * 1024L, BeaconDeviceRandomGenerator.parseSize("4GB"));
        Assert.assertEquals(1L * 1024L * 1024L * 1024L, BeaconDeviceRandomGenerator.parseSize("1GiB"));
    }

    @Test
    public void testParseSizeMegabytes() {
        Assert.assertEquals(100L * 1024L * 1024L, BeaconDeviceRandomGenerator.parseSize("100M"));
        Assert.assertEquals(4096L * 1024L * 1024L, BeaconDeviceRandomGenerator.parseSize("4096MB"));
    }

    @Test
    public void testParseSizeKilobytesAndBytes() {
        Assert.assertEquals(512L * 1024L, BeaconDeviceRandomGenerator.parseSize("512K"));
        Assert.assertEquals(1048576L, BeaconDeviceRandomGenerator.parseSize("1048576"));
    }

    @Test
    public void testHexStringToByteArray() {
        String hex = "000102030a0b0c0d0e0f10";
        byte[] bytes = BeaconDeviceRandomGenerator.hexStringToByteArray(hex);
        Assert.assertEquals(11, bytes.length);
        Assert.assertEquals((byte) 0x00, bytes[0]);
        Assert.assertEquals((byte) 0x01, bytes[1]);
        Assert.assertEquals((byte) 0x02, bytes[2]);
        Assert.assertEquals((byte) 0x0f, bytes[9]);
        Assert.assertEquals((byte) 0x10, bytes[10]);
    }

    @Test
    public void testLocalRngReaderProduces512Bits() {
        LocalRngFallbackReader reader = new LocalRngFallbackReader();
        String noise = reader.getNoise512Bits();
        Assert.assertNotNull(noise);
        // 512 bits = 64 bytes = 128 hex chars
        Assert.assertEquals(128, noise.length());
        byte[] bytes = BeaconDeviceRandomGenerator.hexStringToByteArray(noise);
        Assert.assertEquals(64, bytes.length);
    }

    @Test
    public void testStreamingDeviceEntropyReader() throws Exception {
        // Testa leitura contínua de 2 blocos de 512 bits a partir de 1 único processo
        String dummy512Bits = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        String script = "sh -c \"echo header1; echo header2; echo " + dummy512Bits + "; echo " + dummy512Bits + "\"";

        try (StreamingDeviceEntropyReader reader = new StreamingDeviceEntropyReader(script, 2)) {
            String noise1 = reader.getNoise512Bits();
            Assert.assertEquals(dummy512Bits, noise1);

            String noise2 = reader.getNoise512Bits();
            Assert.assertEquals(dummy512Bits, noise2);
        }
    }
}
