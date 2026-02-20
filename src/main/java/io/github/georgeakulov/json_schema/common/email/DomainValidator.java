/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.georgeakulov.json_schema.common.email;

import java.io.Serial;
import java.io.Serializable;
import java.net.IDN;

/**
 * <p><strong>Domain name</strong> validation routines.</p>
 *
 * <p>
 * This validator provides methods for validating Internet domain names
 * and top-level domains.
 * </p>
 *
 * <p>Domain names are evaluated according
 * to the standards <a href="https://www.ietf.org/rfc/rfc1034.txt">RFC1034</a>,
 * section 3, and <a href="https://www.ietf.org/rfc/rfc1123.txt">RFC1123</a>,
 * section 2.1. No accommodation is provided for the specialized needs of
 * other applications; if the domain name has been URL-encoded, for example,
 * validation will fail even though the equivalent plaintext version of the
 * same name would have passed.
 * </p>
 *
 * <p>
 * Validation is also provided for top-level domains (TLDs) as defined and
 * maintained by the Internet Assigned Numbers Authority (IANA):
 * </p>
 *
 *  removed
 *
 * <p>
 * (<strong>NOTE</strong>: This class does not provide IP address lookup for domain names or
 * methods to ensure that a given domain name matches a specific IP; see
 * {@link java.net.InetAddress} for that functionality.)
 * </p>
 *
 * @since 1.4
 */
public class DomainValidator implements Serializable {

    private static class IDNBUGHOLDER {
        private static final boolean IDN_TOASCII_PRESERVES_TRAILING_DOTS = keepsTrailingDot();
        private static boolean keepsTrailingDot() {
            final String input = "a."; // must be a valid name
            return input.equals(IDN.toASCII(input));
        }
    }

    // Regular expression strings for hostnames (derived from RFC2396 and RFC 1123)

    private static class LazyHolder { // IODH

        /**
         * Singleton instance of this validator, which
         *  doesn't consider local addresses as valid.
         */
        private static final DomainValidator DOMAIN_VALIDATOR = new DomainValidator(false);

        /**
         * Singleton instance of this validator, which does
         *  consider local addresses valid.
         */
        private static final DomainValidator DOMAIN_VALIDATOR_WITH_LOCAL = new DomainValidator(true);

    }

    /** Maximum allowable length ({@value}) of a domain name */
    private static final int MAX_DOMAIN_LENGTH = 253;

    @Serial
    private static final long serialVersionUID = -4407125112880174009L;

    // RFC2396: domainlabel   = alphanum | alphanum *( alphanum | "-" ) alphanum
    // Max 63 characters
    private static final String DOMAIN_LABEL_REGEX = "\\p{Alnum}(?>[\\p{Alnum}-]{0,61}\\p{Alnum})?";

    // RFC2396 toplabel = alpha | alpha *( alphanum | "-" ) alphanum
    // Max 63 characters
    private static final String TOP_LABEL_REGEX = "\\p{Alpha}(?>[\\p{Alnum}-]{0,61}\\p{Alnum})?";

    /**
     * The above instances must only be returned via the getInstance() methods.
     * This is to ensure that the override data arrays are properly protected.
     */

    // RFC2396 hostname = *( domainlabel "." ) toplabel [ "." ]
    // Note that the regex currently requires both a domain label and a top level label, whereas
    // the RFC does not. This is because the regex is used to detect if a TLD is present.
    // If the match fails, input is checked against DOMAIN_LABEL_REGEX (hostnameRegex)
    // RFC1123 sec 2.1 allows hostnames to start with a digit
    private static final String DOMAIN_NAME_REGEX =
            "^(?:" + DOMAIN_LABEL_REGEX + "\\.)+(" + TOP_LABEL_REGEX + ")\\.?$";

    /**
     * Gets the singleton instance of this validator. It will not consider local addresses as valid.
     *
     * @return the singleton instance of this validator.
     */
    public static synchronized DomainValidator getInstance() {
        return LazyHolder.DOMAIN_VALIDATOR;
    }

    /**
     * Gets the singleton instance of this validator, with local validation as required.
     *
     * @param allowLocal Whether local addresses are considered valid.
     * @return the singleton instance of this validator.
     */
    public static synchronized DomainValidator getInstance(final boolean allowLocal) {
        if (allowLocal) {
            return LazyHolder.DOMAIN_VALIDATOR_WITH_LOCAL;
        }
        return LazyHolder.DOMAIN_VALIDATOR;
    }

    /*
     * Tests whether input contains only ASCII. Treats null as all ASCII.
     */
    private static boolean isOnlyASCII(final String input) {
        if (input == null) {
            return true;
        }
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) > 0x7F) { // CHECKSTYLE IGNORE MagicNumber
                return false;
            }
        }
        return true;
    }

    /**
     * Converts potentially Unicode input to punycode. If conversion fails, returns the original input.
     *
     * @param input the string to convert, not null.
     * @return converted input, or original input if conversion fails.
     */
    // Needed by UrlValidator
    static String unicodeToASCII(final String input) {
        if (isOnlyASCII(input)) { // skip possibly expensive processing
            return input;
        }
        try {
            final String ascii = IDN.toASCII(input);
            if (IDNBUGHOLDER.IDN_TOASCII_PRESERVES_TRAILING_DOTS) {
                return ascii;
            }
            final int length = input.length();
            if (length == 0) { // check there is a last character
                return input;
            }
            // RFC3490 3.1. 1)
            // Whenever dots are used as label separators, the following
            // characters MUST be recognized as dots: U+002E (full stop), U+3002
            // (ideographic full stop), U+FF0E (fullwidth full stop), U+FF61
            // (halfwidth ideographic full stop).
            final char lastChar = input.charAt(length - 1); // fetch original last char
            return switch (lastChar) {
                case '\u002E', // "." full stop
                     '\u3002', // ideographic full stop
                     '\uFF0E', // fullwidth full stop
                     '\uFF61'  // halfwidth ideographic full stop
                        -> ascii + "."; // restore the missing stop
                default -> ascii;
            };
        } catch (final IllegalArgumentException e) { // input is not valid
            return input;
        }
    }


    /** Whether to allow local overrides. */
    private final boolean allowLocal;

    /**
     * RegexValidator for matching domains.
     */
    private final RegexValidator domainRegex =
            new RegexValidator(DOMAIN_NAME_REGEX);

    /**
     * RegexValidator for matching a local hostname
     */
    // RFC1123 sec 2.1 allows hostnames to start with a digit
    private final RegexValidator hostnameRegex =
            new RegexValidator(DOMAIN_LABEL_REGEX);


    /*
     * It is vital that instances are immutable. This is because the default instances are shared.
     */

    /**
     * Private constructor.
     */
    private DomainValidator(final boolean allowLocal) {
        this.allowLocal = allowLocal;
    }

    /**
     * Tests whether this instance allow local addresses.
     *
     * @return true if local addresses are allowed.
     * @since 1.7
     */
    public boolean isAllowLocal() {
        return allowLocal;
    }

    /**
     * Tests whether the specified {@link String} parses as a valid domain name with a recognized top-level domain. The parsing is case-insensitive.
     *
     * @param domain the parameter to check for domain name syntax.
     * @return true if the parameter is a valid domain name.
     */
    public boolean isValid(final String domain) {
        if (domain == null) {
            return false;
        }
        final String ascii = unicodeToASCII(domain);
        // hosts must be equally reachable via punycode and Unicode
        // Unicode is never shorter than punycode, so check punycode
        // if domain did not convert, then it will be caught by ASCII
        // checks in the regexes below
        if (ascii.length() > MAX_DOMAIN_LENGTH) {
            return false;
        }
        final String[] groups = domainRegex.match(ascii);
        if (groups != null && groups.length > 0) {
            return true;
        }
        return allowLocal && hostnameRegex.isValid(ascii);
    }

    public boolean isValidHostname(String domain) {
        if(domain == null || domain.isBlank()) {
            return false;
        }

        if(domain.length() > MAX_DOMAIN_LENGTH) {
            return false;
        }

        final String[] groups = domainRegex.match(domain);
        if(groups != null && groups.length > 0) {
            return true;
        }

        return hostnameRegex.isValid(domain);
    }
}