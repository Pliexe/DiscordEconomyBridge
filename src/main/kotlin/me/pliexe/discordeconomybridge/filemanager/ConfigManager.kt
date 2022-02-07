package me.pliexe.discordeconomybridge.filemanager

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import java.io.*
import java.lang.Exception
import java.nio.charset.Charset

class ConfigManager {
    companion object {
        private const val pluginName = "DiscordEconomyBridge"

        fun saveConfig(strConfig: String, file: File)
        {
            try {
                val writer = BufferedWriter(FileWriter(file))
                writer.write(strConfig)
                writer.flush()
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun getConfig(path: String, main: DiscordEconomyBridge, resource: String): Config {
            val file = getConfigFile(path, main)

            if(!file.exists()) {
                prepareFile(file, path, main, resource)
            }



            return Config(file, getConfigContent(file, main)!!, main)
        }

        private fun getConfigFile(path: String, main: DiscordEconomyBridge): File {

            return if (path.contains("/"))
                File("${main.dataFolder}${File.separator}${path.replace("/", File.separator)}")
            else
                File(main.dataFolder, path)
        }

        private fun prepareFile(file: File, path: String, main: DiscordEconomyBridge, resource: String) {
            try {
                file.parentFile.mkdirs()
                file.createNewFile()

                if(resource.isNotEmpty())
                    copyResource(main.getResource(resource), file)

            } catch (e: IOException) {
                main.logger.severe("Unable to create $path, IO Exception $e")
            }
        }

//        private fun prepareConfigString(configString: String, comments: LinkedHashMap<Int, String>): String {
//            val lines = configString.split("\n")
//            val config = StringBuilder("")
//
//
//            lines.forEach { line ->
//
//                if(line.startsWith(pluginName+"_SPACE")) {
//                    config.append("\n")
//                } else if(line.startsWith(pluginName+"_COMMENT")) {
//                    config.append("#" + line.substring(line.indexOf(':') + 3, line.length - 1) + "\n")
//                } else config.append(line + "\n")
//            }
//
//            return config.toString()
//        }

        private fun copyResource(resource: InputStream, file: File) {
            try {
                val out = FileOutputStream(file)

                var length: Int
                val buffer = ByteArray(1024)

                while ((resource.read(buffer)).also { length = it } > 0) {
                    out.write(buffer, 0, length)
                }

                out.close()
                resource.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun getConfigContent(file: File, main: DiscordEconomyBridge): InputStreamReader? {
            if(!file.exists())
                return null

            return try {

                val configStr = FileReader(file).readLines().joinToString("\n")

                val configStream = ByteArrayInputStream(configStr.toByteArray(Charset.forName("UTF-8")))
                InputStreamReader(configStream)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

//        private fun getConfigContent(file: File, main: DiscordEconomyBridge): DataConverted? {
//            if(!file.exists())
//                return null
//
//            try {
////                var commentNum = 0
////                var spaceNum = 0
//
//                var currentLine: String?
//
//                val convertedString = StringBuilder("")
//                val reader = BufferedReader(FileReader(file))
//                val returnData = LinkedHashMap<Int, String>()
//
//                var index = 0
//
//                while((reader.readLine()).also { currentLine = it } != null) {
//
//                    if(currentLine!!.isEmpty()) {
////                        convertedString.append(pluginName + "_SPACE_" + spaceNum + ": ''" + "\n")
////                        spaceNum++
//
//                        returnData[index] = ""
//                    } else if(currentLine!!.startsWith("#")) {
////                        convertedString.append(currentLine!!.replace("\"", "\\\"").replaceFirst("#", pluginName + "_COMMENT_" + commentNum + ": \"") + "\"\n")
////                        commentNum++
//                        returnData[index] = currentLine!!
//                    } else convertedString.append(currentLine + "\n")
//
//                    index++
//                }
//
//
//                val configStr = convertedString.toString()
//                main.logger.info(configStr)
//
//                val configStream = ByteArrayInputStream(configStr.toByteArray(Charset.forName("UTF-8")))
//                val configStreamReader = InputStreamReader(configStream)
//                reader.close()
//                return DataConverted(configStreamReader, returnData)
//
//            }catch (e: IOException) {
//                e.printStackTrace()
//                return null
//            }
//        }
    }
}

//class DataConverted (
//    val configData: InputStreamReader,
//    val commentsAndSpaces: LinkedHashMap<Int, String>
//)