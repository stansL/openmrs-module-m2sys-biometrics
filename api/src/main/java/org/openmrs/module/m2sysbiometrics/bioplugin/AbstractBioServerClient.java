package org.openmrs.module.m2sysbiometrics.bioplugin;

import org.openmrs.module.m2sysbiometrics.M2SysBiometricsConstants;
import org.openmrs.module.m2sysbiometrics.util.M2SysProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import javax.annotation.PostConstruct;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;

public abstract class AbstractBioServerClient extends WebServiceGatewaySupport implements BioServerClient {

    private static final String LOCATION_ID_PROPERTY = M2SysBiometricsConstants.M2SYS_LOCATION_ID;

    @Autowired
    @Qualifier("m2sysbiometrics.jax2b")
    private Jaxb2Marshaller marshaller;

    @Autowired
    private M2SysProperties properties;

//    @Autowired
//    @Qualifier("m2sysbiometrics.messageFactory")
//    private WebServiceMessageFactory messageFactory;

    @PostConstruct
    public void init() {
        setMarshaller(marshaller);
        setUnmarshaller(marshaller);
//        setMessageFactory(messageFactory);
        SoapMessageFactory saajSoapMessageFactory = null;
        try {
            saajSoapMessageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance());
            saajSoapMessageFactory.setSoapVersion(SoapVersion.SOAP_12);
            setMessageFactory(saajSoapMessageFactory);
        } catch (SOAPException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String enroll(String subjectId, String biometricXml) {
        Register register = new Register();
        register.setLocationID(getLocationId());
        register.setID(subjectId);

        register.setBiometricXml(biometricXml);
        RegisterResponse response;
        try {
            response = (RegisterResponse) getResponse(register);
        }
        catch (Exception ex) {
        	return ex.getMessage();
            }
        return response.getRegisterResult();
    }

    @Override
    public String isRegistered(String subjectId) {
        IsRegistered isRegistered = new IsRegistered();
        isRegistered.setID(subjectId);

        IsRegisteredResponse response = (IsRegisteredResponse) getResponse(isRegistered);

        return response.getIsRegisteredResult();
    }

    @Override
    public String changeId(String oldId, String newId) {
        ChangeID changeID = new ChangeID();
        changeID.setNewID(newId);
        changeID.setOldID(oldId);

        ChangeIDResponse response = (ChangeIDResponse) getResponse(changeID);

        return response.getChangeIDResult();
    }

    @Override
    public String update(String subjectId, String biometricXml) {
        Update update = new Update();
        update.setLocationID(getLocationId());
        update.setID(subjectId);

        update.setBiometricXml(biometricXml);

        UpdateResponse response = (UpdateResponse) getResponse(update);

        return response.getUpdateResult();
    }

    @Override
    public String identify(String biometricXml) {
        Identify identify = new Identify();

        identify.setBiometricXml(biometricXml);
        identify.setLocationID(getLocationId());

        IdentifyResponse response = (IdentifyResponse) getResponse(identify);

        return response.identifyResult;
    }

    @Override
    public String delete(String subjectId) {
        DeleteID deleteID = new DeleteID();
        deleteID.setID(subjectId);

        DeleteIDResponse response = (DeleteIDResponse) getResponse(deleteID);
        return response.getDeleteIDResult();
    }

    @Override
    public boolean isServerUrlConfigured() {
        return properties.isGlobalPropertySet(getServerUrlPropertyName());
    }

    protected abstract String getServerUrlPropertyName();

    protected abstract Object getResponse(Object requestPayload);

    protected String getProperty(String propertyName) {
        return properties.getGlobalProperty(propertyName);
    }

    protected String getServiceUrl() {
        return getProperty(getServerUrlPropertyName());
    }

    private int getLocationId() {
        return Integer.parseInt(getProperty(LOCATION_ID_PROPERTY));
    }
}
