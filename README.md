## SBT Keychain Credentials Plugin

This plugin adds `com.xvyg.keychain.Credentials` object _(of the same name as `sbt.Credentials`)_,
bringing capability of obtaining password from Mac OS X Keychain.

### Use Case

Normally you have something like this in your _build.sbt_ or _project/plugins.sbt_:

    credentials ++= Seq(Credentials(Path.userHome / ".ivy2" / ".credentials"))
    
Where `sbt.Credentials` sources these properties from the file:

    realm=Sonatype Nexus Repository Manager
    host=repo.mynexus.com
    user=aleksandrvin
    password=XXXXXX

The problem is **password stored in plain text** on your filesystem. 

#### Fix for SBT 1.X

To remove plain text password from the file, you add _sbt-keychain-credentials_ as
a global plugin by adding this line to your _~/.sbt/1.0/plugins/build.sbt_:
                 
    addSbtPlugin("com.xvyg" % "sbt-keychain-credentials" % "1.0.1")
    
and these lines to your _~/.sbt/1.0/global.sbt_ (with your own path to credentials file):

    import com.xvyg.sbt.keychain.Credentials

    credentials ++= Seq(Credentials(Path.userHome / ".ivy2" / ".credentials", sLog.value))

This will place credentials with password, obtained from your system's keychain, somewhere
close to the head of `credentials` list. So everywhere in your project, where you have
`Credentials(Path.userHome / ".ivy2" / ".credentials")`, you need to check that they
are appended to the list.

This will bring you next warning when you recompile your project:

    $ sbt compile
    [info] Loading settings from build.sbt ...
    [info] Loading global plugins from /Users/aleksandr.vinokurov/.sbt/1.0/plugins
    [info] Updating ProjectRef(uri("file:/Users/aleksandr.vinokurov/.sbt/1.0/plugins/"), "global-plugins")...
    [info] Done updating.
    [info] Loading settings from plugins.sbt ...
    [info] Loading project definition from /Users/aleksandr.vinokurov/Developer/project
    
    ************************************************************************
      Password appears to be stored as plain text in the file!
        /Users/aleksandr.vinokurov/.ivy2/.credentials
    
      Consider removing it from that file and storing in system's Keychain.
      You can use the command below for this:
    
       security add-generic-password -a aleksandrvin -s repo.mynexus.com -w
    ************************************************************************
    
Now you are ready to remove password from that file (_~/.ivy2/.credentials_ in this
example) and securely store it in Mac OS X Keychain. You can do it with the
_Keychain Access_ app or with the command suggested for you in the warning message. In this
example it is:

    security add-generic-password -a aleksandrvin -s repo.mynexus.com -w
    
It will ask you for the password when called.

Now you need to replace that `password=XXXXXXX` line with `password=`. If you remove the
whole line -- you will see sort of the warnings below:

    [info] Reading credentials from /Users/aleksandr.vinokurov/.ivy2/.credentials ...
    [info] Obtaining password from system's keychain ...
    [warn] password not specified in credentials file: /Users/aleksandr.vinokurov/.ivy2/.credentials

Where first two information messages come from `keychain.Credentials` and the later --
from `sbt.Credentials`. It will be ignored by the SBT, but anyway -- it is nicer to keep
the line `password=` in the credentials file to skip those warnings.

You're done. Recompiling your project should go without warnings about that password thing.

#### Fix for SBT 0.13.X

Plugin needs special build for SBT 0.13.X, which is not ready yet. Stay tuned.
