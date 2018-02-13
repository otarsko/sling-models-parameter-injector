package com.taradevko.aem.model.injector;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Enumeration;

import javax.servlet.ServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.apache.sling.models.spi.injectorspecific.AbstractInjectAnnotationProcessor2;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotationProcessor2;
import org.apache.sling.models.spi.injectorspecific.StaticInjectAnnotationProcessorFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import com.taradevko.aem.model.injector.annotation.RequestParameter;

@Component(property = {Constants.SERVICE_RANKING + "=" + Integer.MIN_VALUE})
public class RequestParameterInjector implements Injector, StaticInjectAnnotationProcessorFactory {

    @Override
    public String getName() {
        return "request-parameter";
    }

    @Override
    public Object getValue(final Object adaptable,
                           final String fieldName,
                           final Type type,
                           final AnnotatedElement annotatedElement,
                           final DisposalCallbackRegistry disposalCallbackRegistry) {

        if (adaptable instanceof ServletRequest) {
            final ServletRequest request = (ServletRequest) adaptable;
            String parameterName = null;

            final RequestParameter annotation = annotatedElement.getAnnotation(RequestParameter.class);
            if (annotation != null && StringUtils.isNotBlank(annotation.regexp())) {
                parameterName = findParameterName(request, annotation.regexp());
            }

            if (type instanceof Class<?>) {
                Class<?> fieldClass = (Class<?>) type;
                return getValue(request, fieldClass, StringUtils.defaultString(parameterName, fieldName));
            }
        }
        return null;
    }

    private String findParameterName(final ServletRequest request, final String paramNameRegexp) {
        final Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String parameterName = (String) parameterNames.nextElement();

            if (parameterName.matches(paramNameRegexp)) {
                return parameterName;
            }
        }

        return null;
    }

    private Object getValue(final ServletRequest request, final Class<?> fieldClass, final String fieldName) {
        String parameterValue = request.getParameter(fieldName);
        if (StringUtils.isBlank(parameterValue)) {
            return null;
        }

        if (fieldClass.equals(Integer.class)) {
            try {
                return Integer.parseInt(parameterValue);
            } catch (NumberFormatException ex) {

                //got exception, so not an integer, return null;
                return null;
            }
        }

        return parameterValue;
    }

    @Override
    public InjectAnnotationProcessor2 createAnnotationProcessor(final AnnotatedElement element) {

        // check if the element has the expected annotation
        RequestParameter annotation = element.getAnnotation(RequestParameter.class);
        if (annotation != null) {
            return new RequestParameterAnnotationProcessor(annotation);
        }
        return null;
    }

    private static class RequestParameterAnnotationProcessor extends AbstractInjectAnnotationProcessor2 {

        private final RequestParameter annotation;

        RequestParameterAnnotationProcessor(RequestParameter annotation) {
            this.annotation = annotation;
        }

        @Override
        public Boolean isOptional() {
            return annotation.optional();
        }
    }
}