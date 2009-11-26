/*
 *  Copyright 2008 Hippo.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.frontend.model;

import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.wicket.Session;
import org.hippoecm.frontend.model.properties.JcrPropertyModel;
import org.hippoecm.frontend.model.properties.JcrPropertyValueModel;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.frontend.types.IFieldDescriptor;
import org.hippoecm.frontend.types.ITypeDescriptor;
import org.hippoecm.frontend.validation.ModelPathElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider of {@link JcrPropertyValueModel}s, based on a {@link JcrItemModel} for a
 * {@link Property}.
 */
public class PropertyValueProvider extends AbstractProvider<JcrPropertyValueModel> {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(PropertyValueProvider.class);

    private IFieldDescriptor descriptor;
    private ITypeDescriptor type;

    public PropertyValueProvider(IFieldDescriptor descriptor, ITypeDescriptor type, JcrItemModel<Property> itemModel) {
        super(itemModel);
        this.descriptor = descriptor;
        this.type = type;
    }

    public IFieldDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void addNew() {
        load();

        try {
            int index;
            Value value = createValue();
            Node node = (Node) getItemModel().getParentModel().getObject();
            String relPath = getItemModel().getPath().substring(node.getPath().length() + 1);
            getItemModel().detach();

            if (descriptor.isMultiple()) {
                Value[] oldValues;
                if (node.hasProperty(relPath)) {
                    Property property = node.getProperty(relPath);
                    if (property.getDefinition().isMultiple()) {
                        oldValues = property.getValues();
                    } else {
                        oldValues = new Value[1];
                        oldValues[0] = property.getValue();
                    }
                } else {
                    oldValues = new Value[0];
                }
                index = oldValues.length;

                Value[] newValues = new Value[oldValues.length + 1];
                System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
                newValues[oldValues.length] = value;

                node.setProperty(relPath, newValues);
            } else {
                if (node.hasProperty(relPath)) {
                    log.error("cannot add more than one value to single-valued property");
                    return;
                } else {
                    node.setProperty(relPath, value);
                    index = JcrPropertyValueModel.NO_INDEX;
                }
                node.setProperty(relPath, value);
            }

            elements.addLast(new JcrPropertyValueModel(index, value, new JcrPropertyModel(getItemModel())));
        } catch (RepositoryException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void remove(JcrPropertyValueModel model) {
        load();
        Iterator<JcrPropertyValueModel> iterator = elements.iterator();
        int newIndex = 0;
        while (iterator.hasNext()) {
            JcrPropertyValueModel currentModel = iterator.next();
            if (model.equals(currentModel)) {
                iterator.remove();

                try {
                    if (!getItemModel().exists()) {
                        log.warn("value not found");
                    } else {
                        Property property = (Property) getItemModel().getObject();
                        if (descriptor.isMultiple()) {
                            if (property.getDefinition().isMultiple()) {
                                Value[] oldValues = property.getValues();
                                int index = model.getIndex();
                                if (index >= 0 && index < oldValues.length) {
                                    Value[] newValues = new Value[oldValues.length - 1];
                                    int j = 0;
                                    for (int i = 0; i < oldValues.length; i++) {
                                        if (i == index)
                                            continue;
                                        newValues[j++] = oldValues[i];
                                    }
                                    property.setValue(newValues);
                                } else {
                                    log.warn("index outside of range");
                                }
                            } else {
                                property.remove();
                            }
                        } else {
                            property.remove();
                        }
                    }
                } catch (RepositoryException ex) {
                    log.error(ex.getMessage());
                }
            } else {
                currentModel.setIndex(newIndex++);
            }
        }
    }

    @Override
    public void moveUp(JcrPropertyValueModel model) {
        load();
        int index = model.getIndex();
        if (descriptor.isMultiple() && index > 0) {
            try {
                Property property = (Property) getItemModel().getObject();
                Value[] oldValues = property.getValues();
                Value[] newValues = new Value[oldValues.length];
                System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);

                newValues[index] = oldValues[index - 1];
                newValues[index - 1] = oldValues[index];

                property.setValue(newValues);
            } catch (RepositoryException ex) {
                log.error(ex.getMessage());
            }
        } else {
            log.error("Cannot move first value further up.");
        }
    }

    @Override
    public ModelPathElement getFieldElement(JcrPropertyValueModel model) {
        int index = model.getIndex();
        if (index == -1) {
            index = 0;
        }
        return new ModelPathElement(descriptor, descriptor.getPath(), index);
    }

    @Override
    protected void load() {
        if (elements != null) {
            return;
        }

        elements = new LinkedList<JcrPropertyValueModel>();
        try {
            Node node = (Node) getItemModel().getParentModel().getObject();
            String path = getItemModel().getPath().substring(node.getPath().length() + 1);
            if (getItemModel().exists()) {
                Property property = (Property) getItemModel().getObject();
                if (property.getDefinition().isMultiple()) {
                    Value[] values = property.getValues();
                    for (int index = 0; index < values.length; index++) {
                        if (descriptor.isMultiple()) {
                            addValue(property, values[index], index);
                        } else {
                            Value value = property.getValues()[0];
                            property.remove();
                            property = node.setProperty(path, value);
                            addValue(property, value, JcrPropertyValueModel.NO_INDEX);
                            break;
                        }
                    }
                } else {
                    if (descriptor.isMultiple()) {
                        Value value = property.getValue();
                        property.remove();
                        property = node.setProperty(path, new Value[] { value });
                        addValue(property, value, 0);
                    } else {
                        addValue(property, property.getValue(), JcrPropertyValueModel.NO_INDEX);
                    }
                }
            }
        } catch (RepositoryException ex) {
            log.error(ex.getMessage());
        }
    }

    private Value createValue() throws UnsupportedRepositoryOperationException, RepositoryException {
        ValueFactory factory = ((UserSession) Session.get()).getJcrSession().getValueFactory();
        int propertyType = PropertyType.valueFromName(type.getType());
        switch (propertyType) {
        case PropertyType.BOOLEAN:
            return factory.createValue(false);
        case PropertyType.DATE:
            return factory.createValue(Calendar.getInstance());
        case PropertyType.DOUBLE:
            return factory.createValue(0.0);
        case PropertyType.LONG:
            return factory.createValue(0L);
        case PropertyType.NAME:
            return factory.createValue("", PropertyType.NAME);
        case PropertyType.PATH:
            return factory.createValue("/", PropertyType.PATH);
        case PropertyType.REFERENCE:
            return factory.createValue(UUID.randomUUID().toString(), PropertyType.REFERENCE);
        case PropertyType.STRING:
        case PropertyType.UNDEFINED:
            return factory.createValue("", PropertyType.STRING);
        default:
            return null;
        }
    }

    private void addValue(Property property, Value value, int index) throws RepositoryException {
        String name = property.getName();
        Set<String> excluded = descriptor.getExcluded();
        if (excluded == null || !excluded.contains(name)) {
            elements.addLast(new JcrPropertyValueModel(index, value, new JcrPropertyModel(property)));
        }
    }

}
