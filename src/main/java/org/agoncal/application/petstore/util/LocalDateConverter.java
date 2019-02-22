package org.agoncal.application.petstore.util;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@FacesConverter(value = "localDateConverter")
public class LocalDateConverter implements Converter {

    private static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d/M/yyyy");

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object modelValue) {
        if (modelValue == null) {
            return "";
        }

        if (modelValue instanceof LocalDate) {
            return ((LocalDate) modelValue).format(DATE_FORMAT);
        } else {
            throw new ConverterException(new FacesMessage(modelValue + " is not a valid LocalDateTime"));
        }
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String submittedValue) {
        if (submittedValue == null || submittedValue.isEmpty()) {
            return null;
        }
        return LocalDate.parse(submittedValue, DATE_FORMAT);
    }
}