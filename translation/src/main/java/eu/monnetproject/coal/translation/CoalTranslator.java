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

import eu.monnetproject.config.Configurator;
import eu.monnetproject.lang.Language;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author jmccrae
 */
public class CoalTranslator {

    private final String server, database, username, password;
    private final Set<LangPair> langPairs = new HashSet<LangPair>();

    public CoalTranslator(final Logger logger) throws SQLException {
        final Properties dbConfig = Configurator.getConfig("com.mysql.jdbc");
        if (dbConfig.containsKey("username") && dbConfig.containsKey("password") && dbConfig.containsKey("database")) {
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
            } catch (ClassNotFoundException x) {
                logger.log("MySQL driver not available", x);
            } catch (InstantiationException x) {
                logger.log("MySQL driver not available", x);
            } catch (IllegalAccessException x) {
                logger.log("MySQL driver not available", x);
            }
            this.server = dbConfig.containsKey("server") ? dbConfig.getProperty("server") : "localhost";
            this.database = dbConfig.getProperty("database");
            this.username = dbConfig.getProperty("username");
            this.password = dbConfig.getProperty("password");
            // Check we can connect
            checkForTables();
            logger.log("Using MySQL database");
        } else {
            logger.log("No configuration");
            this.server = this.database = this.username = this.password = null;
            //throw new RuntimeException("dbConfig not found");
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + server + "/" + database + "?user=" + username + "&password=" + password + "&useUnicode=yes&characterEncoding=UTF-8");
    }

    private void checkForTables() throws SQLException {
        final Connection connection = connect();
        final DatabaseMetaData metaData = connection.getMetaData();
        final ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        while (tables.next()) {
            final String name = tables.getString("TABLE_NAME");
            if (name.startsWith("pt_")) {
                final String[] ss = name.split("_");
                langPairs.add(new LangPair(Language.get(ss[1]), Language.get(ss[2])));
            }
        }
        tables.close();
        final ResultSet views = metaData.getTables(null, null, "%", new String[]{"VIEW"});
        while (views.next()) {
            final String name = views.getString("TABLE_NAME");
            if (name.startsWith("pt_")) {
                final String[] ss = name.split("_");
                langPairs.add(new LangPair(Language.get(ss[1]), Language.get(ss[2])));
            }
        }
        views.close();
        connection.close();
    }
    private static final double NEGATIVE_ONE_BILLION = -1e9;

    public Collection<TranslationImpl> translate(String label, Language srcLang, Language trgLang) {
        try {
            final Connection connection = connect();
            final PreparedStatement preparedStatement = connection.prepareStatement("select * from pt_" + srcLang + "_" + trgLang + " where forin=?");
            if (langPairs.contains(new LangPair(srcLang, trgLang))) {
                final String[] tokens = FairlyGoodTokenizer.split(label);
                final StringBuffer partTranslation = new StringBuffer();
                ILOOP:
                for (int i = 0; i < tokens.length;) {
                    String bestCandidate = "";
                    int bestLength = 0;
                    double bestScore = NEGATIVE_ONE_BILLION;
                    for (int j = i + 1; j <= tokens.length; j++) {
                        final String chunk = mkString(tokens, i, j);
                        final ScoredLabel scoredLabel = doTranslation(chunk, preparedStatement);
                        if (scoredLabel != null) {
                            if (scoredLabel.score > bestScore) {
                                bestCandidate = scoredLabel.label;
                                bestScore = scoredLabel.score;
                                bestLength = j - i;
                            }
                        } else {
                            if (j - i == 1) { // OOV word
                                partTranslation.append(chunk).append(" ");
                                i++;
                                continue ILOOP;
                            } else {
                                partTranslation.append(bestCandidate).append(" ");
                                i += bestLength;
                                continue ILOOP;
                            }
                        }
                    }
                    partTranslation.append(bestCandidate).append(" ");
                    i += bestLength;
                }
                preparedStatement.close();
                connection.close();
                return Collections.singleton(new TranslationImpl(label, partTranslation.toString().trim(), srcLang, trgLang));
            } else {
                return Collections.EMPTY_LIST;
            }
        } catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    private ScoredLabel doTranslation(String label, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, label);
        final ResultSet resultSet = preparedStatement.executeQuery();
        ScoredLabel bestLabel = null;
        while (resultSet.next()) {
            final String forin = resultSet.getString("forin").trim().replaceAll("^\\s+", "");
            if (forin.equals(label)) {
                final String translation = resultSet.getString("translation").trim().replaceAll("^\\s+", "");
                final String[] scores = resultSet.getString("scores").trim().split("\\s+");
                double score = 0.0;
                for (String scoreStr : scores) {
                    score += Math.log(Double.parseDouble(scoreStr));
                }
                if (bestLabel == null || score > bestLabel.score) {
                    bestLabel = new ScoredLabel(translation, score);
                }
            }
        }
        return bestLabel;
    }

    private String mkString(String[] tokens, int i, int j) {
        final StringBuilder sb = new StringBuilder();
        for (int n = i; n < j; n++) {
            sb.append(tokens[n]).append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private static class LangPair {

        public final Language srcLang, trgLang;

        public LangPair(Language srcLang, Language trgLang) {
            this.srcLang = srcLang;
            this.trgLang = trgLang;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.srcLang != null ? this.srcLang.hashCode() : 0);
            hash = 89 * hash + (this.trgLang != null ? this.trgLang.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LangPair other = (LangPair) obj;
            if (this.srcLang != other.srcLang && (this.srcLang == null || !this.srcLang.equals(other.srcLang))) {
                return false;
            }
            if (this.trgLang != other.trgLang && (this.trgLang == null || !this.trgLang.equals(other.trgLang))) {
                return false;
            }
            return true;
        }
    }

    private static class ScoredLabel {

        private final String label;
        private final double score;

        public ScoredLabel(String label, double score) {
            this.label = label;
            this.score = score;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 73 * hash + (this.label != null ? this.label.hashCode() : 0);
            hash = 73 * hash + (int) (Double.doubleToLongBits(this.score) ^ (Double.doubleToLongBits(this.score) >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ScoredLabel other = (ScoredLabel) obj;
            if ((this.label == null) ? (other.label != null) : !this.label.equals(other.label)) {
                return false;
            }
            if (Double.doubleToLongBits(this.score) != Double.doubleToLongBits(other.score)) {
                return false;
            }
            return true;
        }
    }
}
