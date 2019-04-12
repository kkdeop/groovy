import groovy.transform.SourceURI
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Logger
import jenkins.model.*

@SourceURI
URI sourceUri

Path scriptLocation = Paths.get(sourceUri)
path_to_file = scriptLocation.getParent()
path_to_file += 'plugins.txt'
path = ''
for (i in path_to_file) {
    path += ("/" + i)
}
String fileContents = new File(path).getText('UTF-8')


def logger = Logger.getLogger("")
def installed = false
def initialized = false
def pluginParameter=fileContents
def plugins = pluginParameter.split()
logger.info("" + plugins)
def instance = Jenkins.getInstance()
def pm = instance.getPluginManager()
def uc = instance.getUpdateCenter()
plugins.each {
    logger.info("Checking " + it)
    if (!pm.getPlugin(it)) {
        logger.info("Looking UpdateCenter for " + it)
        if (!initialized) {
            uc.updateAllSites()
            initialized = true
        }
        def plugin = uc.getPlugin(it)
        if (plugin) {
            logger.info("Installing " + it)
            def installFuture = plugin.deploy()
            while(!installFuture.isDone()) {
                logger.info("Waiting for plugin install: " + it)
                sleep(3000)
            }
            installed = true
        }
    }
}
if (installed) {
    logger.info("Plugins installed, initializing a restart!")
    instance.save()
    instance.restart()
}

