# Lessons learnt
## outputs for commands of exploratory run 
- Checking out changes the `$GITHUB_WORKSPACE` env variable
````bash
uname -a
Linux fv-az562-837 5.15.0-1024-azure #30-Ubuntu SMP Wed Nov 16 23:37:59 UTC 2022 x86_64 x86_64 x86_64 GNU/Linux

java -version
openjdk version "11.0.17" 2022-10-18
OpenJDK Runtime Environment Temurin-11.0.17+8 (build 11.0.17+8)
OpenJDK 64-Bit Server VM Temurin-11.0.17+8 (build 11.0.17+8, mixed mode)

pwd
/home/runner/work/CI-deepimagej-bioimage-io/CI-deepimagej-bioimage-io
````

## Problem running deepimagej headless on arch
```bash
/home/deck/blank_fiji/Fiji.app/ImageJ-linux64 --headless --ij2 --console --run /home/deck/repos/CI-deepimagej-bioimage-io/java_CI_scripts/src/reproduce/test_1_with_deepimagej.clj 'folder="/home/deck/repos/CI-deepimagej-bioimage-io/java_CI_scripts/../models/10.5281/zenodo.5874741/5874742"'
```
- Error message related to not opening the image (but the script checks if it is not null...)
- Also, everything around try and catch, but error by fiji is thrown
```
   The folder is: /home/deck/repos/CI-deepimagej-bioimage-io/java_CI_scripts/../models/10.5281/zenodo.5874741/5874742
--
-- Started testing model: impartial-shrimp
   Name: Neuron Segmentation in EM (Membrane Prediction)
java.lang.NullPointerException
        at javax.swing.MultiUIDefaults.getUIError(MultiUIDefaults.java:130)
        at javax.swing.UIDefaults.getUI(UIDefaults.java:761)
        at javax.swing.UIManager.getUI(UIManager.java:1016)
        at javax.swing.JButton.updateUI(JButton.java:147)
        at javax.swing.AbstractButton.init(AbstractButton.java:2176)
        at javax.swing.JButton.<init>(JButton.java:137)
        at javax.swing.JButton.<init>(JButton.java:110)
        at DeepImageJ_Run.<init>(DeepImageJ_Run.java:135)
        at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
        at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
        at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
        at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
        at java.lang.Class.newInstance(Class.java:442)
        at ij.IJ.runUserPlugIn(IJ.java:235)
        at ij.IJ.runPlugIn(IJ.java:203)
        at ij.Executer.runCommand(Executer.java:152)
        at ij.Executer.run(Executer.java:70)
        at ij.IJ.run(IJ.java:319)
        at ij.IJ.run(IJ.java:409)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at clojure.lang.Reflector.invokeMatchingMethod(Reflector.java:93)
        at clojure.lang.Reflector.invokeStaticMethod(Reflector.java:207)
        at reproduce.test_1_with_deepimagej$test_one_with_deepimagej$fn__31.invoke(NO_SOURCE_FILE:0)
        at reproduce.test_1_with_deepimagej$test_one_with_deepimagej.invokeStatic(NO_SOURCE_FILE:0)
        at reproduce.test_1_with_deepimagej$test_one_with_deepimagej.invoke(NO_SOURCE_FILE:0)
        at reproduce.test_1_with_deepimagej$test_one_with_deepimagej__AMPERSAND__info.invokeStatic(NO_SOURCE_FILE:0)
        at reproduce.test_1_with_deepimagej$test_one_with_deepimagej__AMPERSAND__info.invoke(NO_SOURCE_FILE:0)
        at reproduce.test_1_with_deepimagej$_main.invokeStatic(NO_SOURCE_FILE:0)
        at reproduce.test_1_with_deepimagej$_main.doInvoke(NO_SOURCE_FILE:0)
        at clojure.lang.RestFn.invoke(RestFn.java:397)
        at reproduce.test_1_with_deepimagej$eval53.invokeStatic(NO_SOURCE_FILE:0)
        at reproduce.test_1_with_deepimagej$eval53.invoke(NO_SOURCE_FILE)
        at clojure.lang.Compiler.eval(Compiler.java:6927)
        at clojure.lang.Compiler.eval(Compiler.java:6890)
        at org.scijava.plugins.scripting.clojure.ClojureScriptEngine.eval(ClojureScriptEngine.java:88)
        at org.scijava.plugins.scripting.clojure.ClojureScriptEngine.eval(ClojureScriptEngine.java:63)
        at org.scijava.script.ScriptModule.run(ScriptModule.java:164)
        at org.scijava.module.ModuleRunner.run(ModuleRunner.java:163)
        at org.scijava.module.ModuleRunner.call(ModuleRunner.java:124)
        at org.scijava.module.ModuleRunner.call(ModuleRunner.java:63)
        at org.scijava.thread.DefaultThreadService.lambda$wrap$2(DefaultThreadService.java:225)
        at java.util.concurrent.FutureTask.run(FutureTask.java:266)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        at java.lang.Thread.run(Thread.java:750)

There are no images open
```


# Models from the zoo with multiple weight formats
## onnx and pytorch weights
- affable-shark
- straightforward-crocodile
