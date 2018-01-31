package datadog.opentracing.decorators;

import datadog.trace.common.util.ConfigUtils;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/** Create DDSpanDecorators from a valid configuration */
@Slf4j
public class DDDecoratorsFactory {

  public static final String CONFIG_PATH = "dd-trace-decorators";
  public static String DECORATORS_PACKAGE = "datadog.opentracing.decorators.";

  /**
   * Create decorators from configuration
   *
   * @param decoratorsConfig
   * @return the list of instanciated and configured decorators
   */
  public static List<AbstractDecorator> create(
      final List<DecoratorsConfig.DDSpanDecoratorConfig> decoratorsConfig) {
    final List<AbstractDecorator> decorators = new ArrayList<>();
    for (final DecoratorsConfig.DDSpanDecoratorConfig decoratorConfig : decoratorsConfig) {
      if (decoratorConfig.getType() == null) {
        log.warn("Cannot create decorator without type from configuration {}", decoratorConfig);
        continue;
      }

      // Find class and create
      final Class<?> decoratorClass;
      try {
        decoratorClass = Class.forName(DECORATORS_PACKAGE + decoratorConfig.getType());
      } catch (final ClassNotFoundException e) {
        log.warn(
            "Cannot create decorator as the class {} is not defined. Provided configuration {}",
            decoratorConfig);
        continue;
      }

      AbstractDecorator decorator = null;
      try {
        decorator = (AbstractDecorator) decoratorClass.getConstructor().newInstance();
      } catch (final Exception e) {
        log.warn(
            "Cannot create decorator as we could not invoke the default constructor. Provided configuration {}",
            decoratorConfig);
        continue;
      }

      // Fill with config values
      if (decoratorConfig.getMatchingTag() != null) {
        decorator.setMatchingTag(decoratorConfig.getMatchingTag());
      }
      if (decoratorConfig.getMatchingValue() != null) {
        decorator.setMatchingValue(decoratorConfig.getMatchingValue());
      }
      if (decoratorConfig.getSetTag() != null) {
        decorator.setSetTag(decoratorConfig.getSetTag());
      }
      if (decoratorConfig.getSetValue() != null) {
        decorator.setSetValue(decoratorConfig.getSetValue());
      }

      decorators.add(decorator);
    }
    return decorators;
  }

  public static List<AbstractDecorator> createFromResources() {
    final List<AbstractDecorator> result;
    final DecoratorsConfig config =
        ConfigUtils.loadConfigFromResource(CONFIG_PATH, DecoratorsConfig.class);
    if (config == null) {
      result = createBuiltinDecorators();
    } else {
      result = DDDecoratorsFactory.create(config.getDecorators());
    }
    return result;
  }

  private static List<AbstractDecorator> createBuiltinDecorators() {
    List<AbstractDecorator> builtin = new ArrayList<AbstractDecorator>(8);
    {
      final HTTPComponent httpDecorator1 = new HTTPComponent();
      httpDecorator1.setMatchingTag("component");
      httpDecorator1.setMatchingValue("okhttp");
      builtin.add(httpDecorator1);
    }
    {
      final HTTPComponent httpDecorator2 = new HTTPComponent();
      httpDecorator2.setMatchingTag("component");
      httpDecorator2.setMatchingValue("java-aws-sdk");
      builtin.add(httpDecorator2);
    }
    builtin.add(new ErrorFlag());
    builtin.add(new DBTypeDecorator());
    builtin.add(new DBStatementAsResourceName());
    builtin.add(new OperationDecorator());
    builtin.add(new Status404Decorator());
    builtin.add(new URLAsResourceName());

    return builtin;
  }
}
