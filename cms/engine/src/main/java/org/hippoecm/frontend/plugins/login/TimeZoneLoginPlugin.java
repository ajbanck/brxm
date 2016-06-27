/*
 *  Copyright 2016 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.plugins.login;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.ArrayUtils;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.apache.wicket.util.template.TextTemplate;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.session.PluginUserSession;

public class TimeZoneLoginPlugin extends SimpleLoginPlugin {

    private static final TextTemplate INIT_JS = new PackageTextTemplate(TimeZoneLoginPlugin.class, "timezones-init.js");
    private static final ResourceReference JSTZ_JS = new JavaScriptResourceReference(TimeZoneLoginPlugin.class, "jstz.min.js");

    public static final String SHOW_TIMEZONES_CONFIG_PARAM = "show.timezones";
    public static final String SELECTED_TIMEZONES_CONFIG_PARAM = "selected-timezones";

    public TimeZoneLoginPlugin(final IPluginContext context, final IPluginConfig config) {
        super(context, config);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        if (getPluginConfig().getBoolean(SHOW_TIMEZONES_CONFIG_PARAM)) {
            response.render(JavaScriptReferenceHeaderItem.forReference(JSTZ_JS));
            response.render(OnLoadHeaderItem.forScript(INIT_JS.asString()));
        }
    }

    @Override
    protected LoginPanel createLoginPanel(final String id, final boolean autoComplete, final List<String> locales,
                                          final LoginHandler handler) {
        return new TimeZoneLoginForm(id, autoComplete, locales, handler);
    }

    class TimeZoneLoginForm extends CaptchaForm {

        private static final String TIMEZONE_COOKIE = "tzcookie";
        private static final int TIMEZONE_COOKIE_MAX_AGE = 365 * 24 * 3600; // expire one year from now

        public String selectedTimeZone;

        public TimeZoneLoginForm(final String id, final boolean autoComplete, final List<String> locales, final LoginHandler handler) {
            super(id, autoComplete, locales, handler);

            if (getPluginConfig().getBoolean(SHOW_TIMEZONES_CONFIG_PARAM)) {

                List<String> availableTimeZones = getAvailableTimeZones();

                //Check if user has previously selected a timezone
                String cookieTimeZone = getCookieValue(TIMEZONE_COOKIE);
                if (cookieTimeZone != null && availableTimeZones.contains(cookieTimeZone)) {
                    selectedTimeZone = cookieTimeZone;
                    ((PluginUserSession) getSession()).getClientInfo().getProperties().setTimeZone(
                            TimeZone.getTimeZone(selectedTimeZone));
                }

                //add the timezone dropdown
                DropDownChoice<String> timeZone = new DropDownChoice<>("timezone",
                        new PropertyModel<String>(this, "selectedTimeZone") {
                            @Override
                            public void setObject(final String object) {
                                super.setObject(availableTimeZones.contains(object) ? object : "GMT");
                            }
                        },
                        availableTimeZones,
                        new IChoiceRenderer<String>() {
                            @Override
                            public String getDisplayValue(final String object) {
                                return object;
                            }

                            @Override
                            public String getIdValue(final String object, final int index) {
                                return object;
                            }
                        }
                );

                timeZone.setNullValid(true);

                form.add(new Label("timezone-label", new ResourceModel("timezone-label", "Timezone:")));
                form.add(timeZone);

            } else {
                form.add(new Label("timezone-label").setVisible(false));
                form.add(new Label("timezone").setVisible(false));
            }
        }

        @Override
        protected void loginSuccess() {
            if (selectedTimeZone != null) {
                setCookieValue(TIMEZONE_COOKIE, selectedTimeZone, TIMEZONE_COOKIE_MAX_AGE);
                ((PluginUserSession) getSession()).getClientInfo().getProperties().setTimeZone(
                        TimeZone.getTimeZone(selectedTimeZone));
            }
            super.loginSuccess();
        }

        private List<String> getAvailableTimeZones() {
            String[] timeZones = getPluginConfig().getStringArray(SELECTED_TIMEZONES_CONFIG_PARAM);
            if (ArrayUtils.isEmpty(timeZones)) {
                timeZones = TimeZone.getAvailableIDs();
            }
            return Arrays.asList(timeZones);
        }
    }
}
