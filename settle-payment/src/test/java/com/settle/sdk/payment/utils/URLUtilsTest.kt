package com.settle.sdk.payment.utils

import com.settle.sdk.payment.utils.URLUtils.validateURL
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ValidateURLTest {

    @Test
    fun testValidHttpUrl() {
        val url = "http://account.settle.club"
        assertTrue(validateURL(url))
    }

    @Test
    fun testValidHttpsUrl() {
        val url = "https://account.settle.club"
        assertTrue(validateURL(url))
    }

    @Test
    fun testInvalidSchemeHttpUrl() {
        val url = "ftp://account.settle.club"
        assertFalse(validateURL(url))
    }

    @Test
    fun testInvalidSchemeHttpsUrl() {
        val url = "mailto:example@account.settle.club"
        assertFalse(validateURL(url))
    }

    @Test
    fun testInvalidHostHttpUrl() {
        val url = "http://different.com"
        assertFalse(validateURL(url))
    }

    @Test
    fun testInvalidHostHttpsUrl() {
        val url = "https://another.com"
        assertFalse(validateURL(url))
    }

    @Test
    fun testFileSchemeUrl() {
        val url = "file:///path/to/file"
        assertFalse(validateURL(url))
    }

    @Test
    fun testJavascriptSchemeUrl() {
        val url = "javascript:alert('test')"
        assertFalse(validateURL(url))
    }
}