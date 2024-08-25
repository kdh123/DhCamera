package com.dhkim.dhcamera

import com.dhkim.dhcamera.camera.model.Element
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test() {
        val list = mutableListOf<Element>().apply {
            repeat(10) {
                add(Element.Text(id = "$it", text = "text$it"))
            }
        }

        var element = list[1]
        element = (element as Element.Text).copy(scale = 32f)

        val updateList = list.apply {
            set(1, element)
        }
        println("element : ${updateList[1]}, ${element._scale}")

    }
}