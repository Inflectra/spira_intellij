# SpiraTeam plugin for Intellij
## Setting up your dev environment

1. Install the latest version of Intellij IDEA or update to it.
2. You now have two options for step 2:

Option 1:
* Use github desktop to clone the relevant branch to your computer.

Option 2:
* Download the relevant branch off of our public github repo.
* Extract the zip file into a new folder.

Next:

3. Open Intellij IDEA and click 'Projects' -> 'Open'.
4. Navigate to the folder you just created, select it, and press 'OK'.

<br>

## Debugging

In order to debug the plugin, you'll have to first build the plugin on your machine. To do this, click the Gradle tab on the right side of your IDE.
Then select 'spira_intellij' -> 'Tasks' -> 'intellij' -> and then double click buildPlugin.

Once that is finished, right click 'runIde' under the 'intellij' tasks tab, and then in that menu click 'Debug spira_intellij'. This will run a new instance of your intellij IDE with the plugin enabled on it for you to test out.

To change the version of intellij you want to test it on, change the `platformVersion` property in `gradle.properties`.

To change the versions of intellij the plugin supports, change the `pluginUntilBuild` property in `gradle.properties`.

<br>

### Web pages I found helpful:
* Getting Started with Gradle:
https://plugins.jetbrains.com/docs/intellij/gradle-prerequisites.html

* All about Plugin.xml (Plugin Configuration File): https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html

* All about Creating actions (actions are how users can actually use our plugin): https://plugins.jetbrains.com/docs/intellij/working-with-custom-actions.html 

* Plugin overview page: https://plugins.jetbrains.com/docs/marketplace/plugin-overview-page.html

* Jetbrains support page: https://intellij-support.jetbrains.com/hc/en-us#


<br>


### Issues found while updating to Intellij 2022.2

Several classes were reporting compile errors after I moved them around in the folder hierarchy to fit the gradle folder standards. Thankfully Intellij has a very handy `Find in files` feature. This allowed me to search for `com.inflectra.` within package and import statements, and replace that with the new source path to fix those errors.

Another issue I ran into was that a method call using the intellij API had completely changed since the last version and was throwing an error in the debug log. The error message helpfully included a statement saying `replace (some method call) with (some other, kind of similar looking method call)`. Not realizing these were actual instructions I should follow, I spent a while searching around online to see if someone else had posted about this issue and gotten a good answer. No luck. I ran debug again and actually followed the instructions from the error message. After using `Find in files` again to replace the method call, five errors were fixed.

The last error reported that an action being referenced in `plugin.xml` was not registered. This confused me because all of the actions were registered in the exact same way that an example plugin registered it's own. After searching around online I found a single post on jetbrains support talking about the error... from 2011. Long story short, the plugin previously needed to reference the actions within the action group declaration before registering the actions, which now actually causes an error. I removed the unnecessary code and then the error was fixed.

