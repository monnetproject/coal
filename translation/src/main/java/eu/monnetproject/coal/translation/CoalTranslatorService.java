/**
 * *******************************************************************************
 * Copyright (c) 2011, Monnet Project All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the Monnet Project nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *******************************************************************************
 */
package eu.monnetproject.coal.translation;

import eu.monnetproject.lang.Language;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collection;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jmccrae
 */
public class CoalTranslatorService extends HttpServlet {
    
    private CoalTranslator translator;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        final ServletContext context = config.getServletContext();
        try {
            translator = new CoalTranslator(new Logger() {
                @Override
                public void log(String message) {
                    context.log(message);
                }
                
                @Override
                public void log(String message, Throwable t) {
                    context.log(message, t);
                }
            });
        } catch (SQLException x) {
            throw new ServletException(x);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String srcLang = req.getParameter("src");
        final String trgLang = req.getParameter("trg");
        final String label = req.getParameter("label");
        if (srcLang == null || trgLang == null || label == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required parameters are 'src', 'trg', 'label'");
            return;
        }
        final Collection<TranslationImpl> translations = translator.translate(label, Language.get(srcLang), Language.get(trgLang));
        if (translations.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No translations!");
        } else {
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF-8");
            final PrintWriter out = resp.getWriter();
            for (TranslationImpl translation : translations) {
                out.println(translation.getLabel());
            }
            out.flush();
            out.close();
        }
    }
}
