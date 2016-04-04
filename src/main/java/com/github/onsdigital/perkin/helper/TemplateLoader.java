package com.github.onsdigital.perkin.helper;

import com.github.davidcarboni.httpino.Serialiser;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.json.SurveyTemplate;
import com.github.onsdigital.perkin.transform.Audit;
import com.github.onsdigital.perkin.transform.TemplateNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

/**
 * A class for loading and maintaining a set of cached templates from disk
 *
 * Created by ian on 30/03/2016.
 */
@Slf4j
public class TemplateLoader {

    private static final TemplateLoader INSTANCE = new TemplateLoader();

    private Map<String, String> templates;

    private Audit audit = Audit.getInstance();

    private TemplateLoader() {
        templates = new HashMap<>();
    }

    public static TemplateLoader getInstance() {
        return INSTANCE;
    }

    public String getPdfTemplate(Survey survey) throws TemplateNotFoundException {
        return this.getTemplate("templates/" + survey.getId() + "."
                + survey.getCollection().getInstrumentId() + ".pdf.fo");
    }

    public SurveyTemplate getSurveyTemplate(Survey survey) throws TemplateNotFoundException {

        String json = this.getTemplate("templates/" + survey.getId() + "."
                + survey.getCollection().getInstrumentId() + ".survey.json");

        return Serialiser.deserialise(json, SurveyTemplate.class);
    }

    public String getTemplate(String templateFilename) throws TemplateNotFoundException {

        //only time if we load the template
        Timer timer = null;

        String template = null;

        try {
            //only load a template once
            template = templates.get(templateFilename);

            if (template == null) {

                timer = new Timer("template.load.");
                timer.addInfo(templateFilename);

                template = FileHelper.loadFile(templateFilename);
                templates.put(templateFilename, template);

                timer.stopStatus(200);

                log.debug("TEMPLATE|storing template: " + templateFilename);
            }
        } catch (IOException e) {
            if (timer != null) {
                timer.stopStatus(500, e);
            }
            throw new TemplateNotFoundException("problem loading template: " + templateFilename);
        } finally {
            audit.increment(timer);
        }

        return template;
    }
}
