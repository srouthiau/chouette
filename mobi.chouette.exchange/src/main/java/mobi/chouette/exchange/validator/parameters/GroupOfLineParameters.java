package mobi.chouette.exchange.validator.parameters;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import lombok.Data;
import mobi.chouette.model.GroupOfLine;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class GroupOfLineParameters {

	@XmlTransient
	public static String[] fields = { "Objectid", "Name", "RegistrationNumber"} ;
	
	static {
		ValidationParametersUtil.addFieldList(GroupOfLine.class.getSimpleName(), Arrays.asList(fields));
	}

	@XmlElement(name = "objectid")
	private FieldParameters objectid;

	@XmlElement(name = "name")
	private FieldParameters name;

	@XmlElement(name = "registration_number")
	private FieldParameters registrationNumber;

}