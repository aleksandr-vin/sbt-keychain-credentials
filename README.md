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
    
To remove plain text password from that file, you add _sbt-keychain-credentials_ as
a global plugin by adding these lines to your _~/.sbt/1.0/plugins/build.sbt_:
                 
    addSbtPlugin("com.xvyg" % "sbt-keychain-credentials" % "1.0.0")
    
    resolvers += Resolver.url("bintray-aleksandrvin-sbt-plugins", url("http://dl.bintray.com/aleksandrvin/sbt-plugins"))(Resolver.ivyStylePatterns)

and then you import its `Credentials` object:
 
    import com.xvyg.sbt.keychain.Credentials
    
everywhere you have one from `sbt`.

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
    
It will ask you for the password when called. And don't forget to remove password line from
your file.

Recompiling the project should not warn you anymore about the password, then you are done.
