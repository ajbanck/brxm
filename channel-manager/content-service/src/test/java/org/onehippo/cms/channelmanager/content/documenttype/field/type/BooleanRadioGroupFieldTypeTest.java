/*
 * Copyright 2019 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.cms.channelmanager.content.documenttype.field.type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.Value;

import org.hippoecm.repository.util.JcrUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onehippo.cms.channelmanager.content.document.model.FieldValue;
import org.onehippo.cms.channelmanager.content.documenttype.ContentTypeContext;
import org.onehippo.cms.channelmanager.content.documenttype.field.FieldTypeContext;
import org.onehippo.cms.channelmanager.content.documenttype.model.DocumentType;
import org.onehippo.cms.channelmanager.content.documenttype.util.NamespaceUtils;
import org.onehippo.cms.channelmanager.content.error.BadRequestException;
import org.onehippo.cms.channelmanager.content.error.ErrorInfo;
import org.onehippo.cms.channelmanager.content.error.ErrorInfo.Reason;
import org.onehippo.forge.selection.frontend.plugin.Config;
import org.onehippo.repository.mock.MockNode;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({JcrUtils.class, NamespaceUtils.class})
public class BooleanRadioGroupFieldTypeTest {

    private static final String PROPERTY = "test:id";

    @Before
    public void setup() {
        PowerMock.mockStatic(JcrUtils.class);
        PowerMock.mockStatic(NamespaceUtils.class);
    }

    @Test
    public void testFieldConfig() {
        FieldTypeContext fieldTypeContext = getFieldTypeContext();
        expect(fieldTypeContext.getStringConfig(Config.TRUE_LABEL)).andReturn(Optional.of("Happy"));
        expect(fieldTypeContext.getStringConfig(Config.FALSE_LABEL)).andReturn(Optional.of("Sad"));

        replayAll();

        BooleanRadioGroupFieldType booleanRadioGroupFieldType = new BooleanRadioGroupFieldType();
        booleanRadioGroupFieldType.init(fieldTypeContext);

        assertThat(booleanRadioGroupFieldType.getTrueLabel(), equalTo("Happy"));
        assertThat(booleanRadioGroupFieldType.getFalseLabel(), equalTo("Sad"));
    }

    @Test
    public void testFieldConfigDefaultValues() {
        FieldTypeContext fieldTypeContext = getFieldTypeContext();
        expect(fieldTypeContext.getStringConfig(Config.TRUE_LABEL)).andReturn(Optional.empty());
        expect(fieldTypeContext.getStringConfig(Config.FALSE_LABEL)).andReturn(Optional.of(""));

        replayAll();

        BooleanRadioGroupFieldType booleanRadioGroupFieldType = new BooleanRadioGroupFieldType();
        booleanRadioGroupFieldType.init(fieldTypeContext);

        assertThat(booleanRadioGroupFieldType.getTrueLabel(), equalTo("true"));
        assertThat(booleanRadioGroupFieldType.getFalseLabel(), equalTo("false"));
    }

    private FieldTypeContext getFieldTypeContext() {
        final ContentTypeContext parentContext = createMock(ContentTypeContext.class);
        expect(parentContext.getDocumentType()).andReturn(new DocumentType());
        expect(parentContext.getResourceBundle()).andReturn(Optional.empty());

        final FieldTypeContext fieldContext = createMock(FieldTypeContext.class);
        expect(fieldContext.getName()).andReturn("myproject:booleanradiogroup");
        expect(fieldContext.getValidators()).andReturn(Collections.emptyList());
        expect(fieldContext.isMultiple()).andReturn(false).anyTimes();
        expect(fieldContext.getEditorConfigNode()).andReturn(Optional.empty()).anyTimes();
        expect(fieldContext.getParentContext()).andReturn(parentContext).anyTimes();

        return fieldContext;
    }

    @Test
    public void writeToSingleBoolean() throws Exception {
        final Node node = MockNode.root();
        final PrimitiveFieldType fieldType = new BooleanRadioGroupFieldType();
        final Boolean oldValue = Boolean.TRUE;
        final Boolean newValue = Boolean.FALSE;

        fieldType.setId(PROPERTY);
        node.setProperty(PROPERTY, oldValue);

        try {
            fieldType.writeTo(node, Optional.empty());
            fail("Must not be missing");
        } catch (final BadRequestException e) {
            assertThat(((ErrorInfo) e.getPayload()).getReason(), equalTo(Reason.INVALID_DATA));
        }
        assertThat(node.getProperty(PROPERTY).getString(), equalTo("" + oldValue));

        try {
            fieldType.writeTo(node, Optional.of(Collections.emptyList()));
            fail("Must have 1 entry");
        } catch (final BadRequestException e) {
            assertThat(((ErrorInfo) e.getPayload()).getReason(), equalTo(Reason.INVALID_DATA));
        }

        try {
            fieldType.writeTo(node, Optional.of(Arrays.asList(valueOf("true"), valueOf("false"))));
            fail("Must have 1 entry");
        } catch (final BadRequestException e) {
            assertThat(((ErrorInfo) e.getPayload()).getReason(), equalTo(Reason.INVALID_DATA));
        }

        fieldType.writeTo(node, Optional.of(listOf(valueOf("" + newValue))));
        assertThat(node.getProperty(PROPERTY).getString(), equalTo("" + newValue));
    }

    @Test
    public void writeIncorrectValueDoesNotOverwriteExistingValue() throws Exception {
        final Node node = MockNode.root();
        final PrimitiveFieldType fieldType = new BooleanFieldType();
        final Boolean oldValue = Boolean.TRUE;
        final String invalidValue = "foo";

        fieldType.setId(PROPERTY);
        node.setProperty(PROPERTY, oldValue);

        fieldType.writeTo(node, Optional.of(listOf(valueOf(invalidValue))));
        assertThat(node.getProperty(PROPERTY).getBoolean(), equalTo(oldValue));
    }

    @Test
    public void writeIncorrectValuesDoesNotOverwriteExistingValues() throws Exception {
        final Node node = MockNode.root();
        final PrimitiveFieldType fieldType = new BooleanFieldType();

        fieldType.setId(PROPERTY);
        fieldType.setMultiple(true);
        fieldType.setMaxValues(2);

        final Boolean oldValue1 = Boolean.TRUE;
        final Boolean oldValue2 = Boolean.TRUE;
        node.setProperty(PROPERTY, new String[]{oldValue1 + "", oldValue2 + ""}, PropertyType.BOOLEAN);

        final String invalidValue1 = "foo";
        final List<FieldValue> es = Arrays.asList(valueOf(invalidValue1), valueOf(oldValue2 + ""));
        fieldType.writeTo(node, Optional.of(es));

        final Value[] values = node.getProperty(PROPERTY).getValues();
        assertThat(values[0].getBoolean(), equalTo(oldValue1));
        assertThat(values[1].getBoolean(), equalTo(oldValue2));
    }


    private static List<FieldValue> listOf(final FieldValue value) {
        return Collections.singletonList(value);
    }

    private static FieldValue valueOf(final String value) {
        return new FieldValue(value);
    }
}
