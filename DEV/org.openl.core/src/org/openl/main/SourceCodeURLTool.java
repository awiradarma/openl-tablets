/*
 * Created on Dec 23, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.main;

import org.openl.exception.OpenLException;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.syntax.ISyntaxNode;
import org.openl.util.StringTool;
import org.openl.util.StringUtils;
import org.openl.util.text.ILocation;
import org.openl.util.text.TextInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author snshor
 */
public class SourceCodeURLTool implements SourceCodeURLConstants {

    static public String makeSourceLocationURL(ILocation location, IOpenSourceCodeModule module) {
        final Logger log = LoggerFactory.getLogger(SourceCodeURLTool.class);

        if (module != null && StringUtils.isEmpty(module.getUri(0))) {
            return StringUtils.EMPTY;
        }

        int position = 0;

        int start = -1, end = -1;

        String lineInfo = null;

        if (location != null && location.isTextLocation()) {
            String src = module.getCode();
            TextInfo info = new TextInfo(src);
            try {
                start = location.getStart().getAbsolutePosition(info) + module.getStartPosition();
                end = location.getEnd().getAbsolutePosition(info) + module.getStartPosition();
            } catch (UnsupportedOperationException e) {
                log.warn("Cannot make source location URL", e);
            }

            lineInfo = START + "=" + start + QSEP + END + "=" + end;

        }

        if (module == null) {
            return "NO_MODULE";
        }

        String moduleUri = module.getUri(position);

        String suffix = moduleUri.indexOf(QSTART) < 0 ? QSTART : QSEP;

        String url = moduleUri;
        if (lineInfo != null) {
            url += suffix + lineInfo;
            suffix = QSEP;
        } else if (location != null) {
            url += suffix + location;
            suffix = QSEP;
        }

        return url;
    }

    static void parseQuery(String query, Map<String, String> map) {
        if (query == null) {
            return;
        }

        StringTokenizer st = new StringTokenizer(query, QSEP);

        while (st.hasMoreTokens()) {
            String pair = st.nextToken();

            int idx = pair.indexOf('=');

            if (idx < 0) {
                map.put(pair, "");
            } else {
                String key = pair.substring(0, idx);
                String value = StringTool.decodeURL(pair.substring(idx + 1, pair.length()));
                map.put(key, value);
            }
        }

    }

    static public Map<String, String> parseUrl(String urls) {
        Map<String, String> map = new HashMap<String, String>();

        try {
            URL url = new URL(urls);
            String file = url.getFile();
            String protocol = url.getProtocol();
            String host = url.getHost();
            String query = url.getQuery();

            int indexQuestionMark = file.indexOf('?');
            file = indexQuestionMark < 0 ? file : file.substring(0, indexQuestionMark);
            file = StringTool.decodeURL(file);

            map.put(PROTOCOL, protocol);
            map.put(HOST, host);
            map.put(FILE, file);

            parseQuery(query, map);
        } catch (MalformedURLException e) {
            map.put(ERROR, e.getMessage());
        }

        return map;
    }

    static public void printCodeAndError(ILocation location, IOpenSourceCodeModule module, PrintWriter pw) {

        if (location == null) {
            return;
        }

        if (!location.isTextLocation()) {
            // stream.println(" at " + location);
            return;
        }

        String src = module.getCode();
        TextInfo info = new TextInfo(src);
        String[] lines = StringTool.splitLines(src);

        // position = location.getStart().getAbsolutePosition(info);

        pw.println("Openl Code Fragment:");
        pw.println("=======================");

        int line1 = location.getStart().getLine(info);
        int column1 = location.getStart().getColumn(info, 1);

        int line2 = location.getEnd().getLine(info);
        int column2 = location.getEnd().getColumn(info, 1);

        int start = Math.max(line1 - 2, 0);

        int end = Math.min(start + 4, lines.length);

        for (int i = start; i < end; ++i) {
            String line = StringTool.untab(lines[i], 2);
            pw.println(line);
            if (i == line1) {
                StringBuilder buf = new StringBuilder(Math.max(column1, column2) + 5);
                StringTool.append(buf, ' ', column1);
                int col2 = line1 == line2 ? column2 + 1 : line.length();

                StringTool.append(buf, '^', col2 - column1);
                pw.println(buf.toString());
            }
        }
        pw.println("=======================");

    }

    static public void printSourceLocation(ILocation location, IOpenSourceCodeModule module, PrintWriter pw) {

        String url = SourceCodeURLTool.makeSourceLocationURL(location, module);

        if (!StringUtils.isEmpty(url)) {
            pw.println(SourceCodeURLConstants.AT_PREFIX + url);
        }
    }

    static public void printSourceLocation(OpenLException error, PrintWriter pw) {
        printSourceLocation(error.getLocation(), error.getSourceModule(), pw);
    }

    static public void printSourceLocation(ISyntaxNode node, PrintWriter pw) {
        printSourceLocation(node.getSourceLocation(), node.getModule(), pw);
    }

}
