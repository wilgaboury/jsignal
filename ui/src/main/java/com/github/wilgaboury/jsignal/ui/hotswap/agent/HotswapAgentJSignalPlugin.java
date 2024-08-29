package com.github.wilgaboury.jsignal.ui.hotswap.agent;

import com.github.wilgaboury.jsignal.ui.Renderable;
import org.hotswap.agent.annotation.Init;
import org.hotswap.agent.annotation.LoadEvent;
import org.hotswap.agent.annotation.OnClassLoadEvent;
import org.hotswap.agent.annotation.Plugin;
import org.hotswap.agent.command.Scheduler;
import org.hotswap.agent.javassist.CannotCompileException;
import org.hotswap.agent.javassist.CtClass;
import org.hotswap.agent.javassist.CtConstructor;
import org.hotswap.agent.logging.AgentLogger;
import org.hotswap.agent.util.PluginManagerInvoker;

/**
 * Important caveat is that classes cannot be renamed
 */
@Plugin(
  name = "JSignalPlugin",
  description = "Reactive java UI Plugin For Component Hot Swapping",
  testedVersions = {"1.4.1"},
  expectedVersions = {"1.4.1"}
)
public class HotswapAgentJSignalPlugin {
  private static final AgentLogger logger = AgentLogger.getLogger(HotswapAgentJSignalPlugin.class);

  @Init
  Scheduler scheduler;

  @Init
  ClassLoader classLoader;

  @Init
  public void init() {
    logger.info("initializing ui hotswap plugin");
  }

  @OnClassLoadEvent(classNameRegexp = "com.github.wilgaboury.jsignal.ui.hotswap.HotswapRerenderService")
  public static void instrumentInitialization(CtClass ct) throws CannotCompileException {
    for (CtConstructor constructor : ct.getDeclaredConstructors()) {
      constructor.insertAfter(PluginManagerInvoker.buildInitializePlugin(HotswapAgentJSignalPlugin.class));
    }
  }

  @OnClassLoadEvent(classNameRegexp = ".*", events = LoadEvent.REDEFINE)
  public void rerenderComponents(CtClass ct, Class<?> clazz) {
    // TODO: check if field has been added, if so parent needs to be reloaded or field will be null
    if (Renderable.class.isAssignableFrom(clazz)) {
      scheduler.scheduleCommand(new RerenderCommand(classLoader, ct.getName()), 100);
    }
  }
}
