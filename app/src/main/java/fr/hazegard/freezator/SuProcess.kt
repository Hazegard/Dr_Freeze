package fr.hazegard.freezator

import android.util.Log
import fr.hazegard.freezator.exception.NotRootException
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*

/**
 * Created by maxime on 01/03/18.
 */
class SuProcess {
    private val su: Process = getSuProcess()
    private val os = DataOutputStream(su.outputStream)
    private val osRes = DataInputStream(su.inputStream)

    fun enablePackage(packageName: String): String {
//        os.writeBytes("pm enable $packageName && echo \"EOF\"\n")
//        os.flush()
        writeInput("pm enable $packageName")
        return readOutput()
    }

    fun disablePackage(packageName: String): String {
//        os.writeBytes("pm disable $packageName && echo \"EOF\"\n")
//        os.flush()
        writeInput("pm disable $packageName")
        return readOutput()
    }

    fun listDisabledPackages(): List<String> {
//        os.writeBytes("pm list packages -d | cut -d ':' -f 2 && echo \"EOF\"\n")
//        os.flush()
        writeInput("pm list packages -d | cut -d ':' -f 2")
        val buffer = BufferedReader(osRes.bufferedReader())
        val disabledPackages = ArrayList<String>()
        var line = buffer.readLine()
        while ("EOF" != line) {
            disabledPackages.add(line)
            line = buffer.readLine()
        }
        return disabledPackages
    }

    @Throws
    private fun getSuProcess(): Process {
        try {
            val su: Process = Runtime.getRuntime().exec("su")
            val (os, osRes) = getDataStream(su)
            os.writeBytes("id -u\n")
            os.flush()
            val currUid: String? = BufferedReader(osRes.bufferedReader()).readLine()
//            assert(currUid?.trim()?.toInt() == 0) {
//                "No Su process"
//            }
            if (currUid == null || currUid.trim().toInt() != 0) {
                throw NotRootException("No Su process")
            } else {
                Log.d("Process", "su process granted")
                return su
            }
        } catch (e: IOException) {
            throw NotRootException("No Su process")
        }
    }

    private fun getDataStream(process: Process): Pair<DataOutputStream, DataInputStream> {
        val dos = DataOutputStream(process.outputStream)
        val dis = DataInputStream(process.inputStream)
        return Pair(dos, dis)
    }

    private fun readOutput(): String {
        val buffer = BufferedReader(osRes.bufferedReader())
        val response = StringBuffer()
        var line = buffer.readLine()
        while ("EOF" != line) {
            response.append(line)
            response.append("\n")
            line = buffer.readLine()
        }
        return response.toString().trim()
    }

    private fun writeInput(input: String) {
        os.writeBytes("$input && echo \"EOF\"\n\" ")
        os.flush()
    }
}