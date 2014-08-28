package org.osiam.resources.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osiam.resources.scim.Extension;
import org.osiam.resources.scim.Extension.Field;
import org.osiam.storage.dao.ExtensionDao;
import org.osiam.storage.entities.ExtensionEntity;
import org.osiam.storage.entities.ExtensionFieldEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * HTTP Api for extensions. You can get extension informations.
 */
@Controller
@RequestMapping(value = "/osiam")
@Transactional
public class OsiamController {

    @Inject
    private ExtensionDao dao;

    @RequestMapping(value="/Extensiontypes", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Map<String, String> getTypes(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Extension> extensions = fromEntity(dao.getAllExtensions());

        Map<String, String> result = new HashMap<String, String>();

        for(Extension extension : extensions){
            for(Entry<String, Field> fieldEntry : extension.getFields().entrySet()){
                final String urnAndField = generateUniqueKey(extension, fieldEntry);
                final String type = fieldEntry.getValue().getType().getName();

                result.put(urnAndField, type);
            }
        }

        return result;
    }

    private String generateUniqueKey(Extension extension, Entry<String, Field> fieldEntry) {
        return extension.getUrn() + "|" + fieldEntry.getKey();
    }

    public List<Extension> fromEntity(List<ExtensionEntity> entities){
        List<Extension> result = new ArrayList<Extension>();

        for(ExtensionEntity entity : entities){
            Extension.Builder builder = new Extension.Builder(entity.getUrn());
            setField(entity, builder);

            result.add(builder.build());
        }

        return result;
    }

    private void setField(ExtensionEntity entity, Extension.Builder builder) {
        for(ExtensionFieldEntity fieldEntity : entity.getFields()){
            switch (fieldEntity.getType().getName()) {
            case "STRING":
                builder.setField(fieldEntity.getName(), "null");
                break;
            case "INTEGER":
                builder.setField(fieldEntity.getName(), BigInteger.ZERO);
                break;
            case "DECIMAL":
                builder.setField(fieldEntity.getName(), BigDecimal.ZERO);
                break;
            case "BOOLEAN":
                builder.setField(fieldEntity.getName(), false);
                break;
            case "DATE_TIME":
                builder.setField(fieldEntity.getName(), new Date(0L));
                break;
            case "BINARY":
                builder.setField(fieldEntity.getName(), ByteBuffer.wrap(new byte[]{}));
                break;
            case "REFERENCE":
                try {
                    builder.setField(fieldEntity.getName(), new URI("http://www.osiam.org"));
                } catch (URISyntaxException e) {
                    throw new IllegalStateException(e);
                }
                break;
            default:
                throw new IllegalArgumentException("Type " + fieldEntity.getType().getName() + " does not exist");
            }
        }
    }
}
