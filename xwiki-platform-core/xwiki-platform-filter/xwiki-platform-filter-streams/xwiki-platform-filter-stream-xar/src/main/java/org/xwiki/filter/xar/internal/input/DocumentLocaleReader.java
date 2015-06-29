/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.filter.xar.internal.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.event.xwiki.XWikiWikiDocumentFilter;
import org.xwiki.filter.xar.input.XARInputProperties;
import org.xwiki.filter.xar.internal.XARAttachmentModel;
import org.xwiki.filter.xar.internal.XARClassModel;
import org.xwiki.filter.xar.internal.XARDocumentModel;
import org.xwiki.filter.xar.internal.XARFilterUtils.EventParameter;
import org.xwiki.filter.xar.internal.XARObjectModel;
import org.xwiki.filter.xar.internal.input.AttachmentReader.WikiAttachment;
import org.xwiki.filter.xar.internal.input.ClassReader.WikiClass;
import org.xwiki.filter.xar.internal.input.WikiObjectReader.WikiObject;
import org.xwiki.filter.xml.internal.input.XMLInputFilterStreamUtils;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.xar.internal.model.XarDocumentModel;

/**
 * @version $Id$
 * @since 6.2M1
 */
@Component(roles = DocumentLocaleReader.class)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DocumentLocaleReader extends AbstractReader
{
    @Inject
    @Named("relative")
    private EntityReferenceResolver<String> relativeResolver;

    @Inject
    private XARXMLReader<WikiObjectReader.WikiObject> objectReader;

    @Inject
    private XARXMLReader<ClassReader.WikiClass> classReader;

    @Inject
    private XARXMLReader<AttachmentReader.WikiAttachment> attachmentReader;

    private XARInputProperties properties;

    private String currentLegacySpace;

    private String currentLegacyDocument;

    private EntityReference previousSpaceReference;

    private EntityReference currentSpaceReference;

    private EntityReference currentDocumentReference;

    private Locale currentDocumentLocale;

    private boolean localeFromLegacy = true;

    private String currentDocumentRevision;

    private FilterEventParameters currentDocumentParameters = new FilterEventParameters();

    private FilterEventParameters currentDocumentLocaleParameters = new FilterEventParameters();

    private FilterEventParameters currentDocumentRevisionParameters = new FilterEventParameters();

    private boolean sentBeginWikiSpace;

    private boolean sentBeginWikiDocument;

    private boolean sentBeginWikiDocumentLocale;

    private boolean sentBeginWikiDocumentRevision;

    private WikiClass currentClass = new WikiClass();

    private Queue<WikiObject> currentObjects = new LinkedList<WikiObject>();

    private Queue<WikiAttachment> currentAttachments = new LinkedList<WikiAttachment>();

    public void setProperties(XARInputProperties properties)
    {
        this.properties = properties;
    }

    public EntityReference getCurrentSpaceReference()
    {
        return this.currentSpaceReference;
    }

    public EntityReference getCurrentDocumentReference()
    {
        return this.currentDocumentReference;
    }

    public boolean isSentBeginWikiSpace()
    {
        return this.sentBeginWikiSpace;
    }

    private void resetDocument()
    {
        if (!this.sentBeginWikiSpace) {
            // Space was not sent after all so let's forget it
            this.currentSpaceReference = null;
            this.currentLegacySpace = null;
        }

        this.currentDocumentReference = null;
        this.currentLegacyDocument = null;
        this.currentDocumentLocale = null;
        this.currentDocumentRevision = null;

        this.currentDocumentParameters = new FilterEventParameters();
        this.currentDocumentLocaleParameters = new FilterEventParameters();
        this.currentDocumentRevisionParameters = new FilterEventParameters();

        this.sentBeginWikiDocument = false;
        this.sentBeginWikiDocumentLocale = false;
        this.sentBeginWikiDocumentRevision = false;
    }

    private boolean canSendBeginWikiSpace(boolean force)
    {
        return !this.sentBeginWikiSpace && (force || this.properties.getEntities() == null);
    }

    private void sendBeginWikiSpace(XARInputFilter proxyFilter, boolean force) throws FilterException
    {
        if (canSendBeginWikiSpace(force)) {
            int previousSize = this.previousSpaceReference != null ? this.previousSpaceReference.size() : 0;
            int size = this.currentSpaceReference != null ? this.currentSpaceReference.size() : 0;

            int diff = size - previousSize;

            if (diff > 0) {
                // TODO: opens what needs to be opened
                for (EntityReference spaceReference = this.currentSpaceReference; diff > 0 && spaceReference != null; spaceReference =
                    spaceReference.getParent(), --diff) {
                    proxyFilter.beginWikiSpace(spaceReference.getName(), FilterEventParameters.EMPTY);
                    this.sentBeginWikiSpace = true;
                    this.previousSpaceReference = this.currentSpaceReference;
                }
            }
        }
    }

    private void sendEndWikiSpace(XARInputFilter proxyFilter) throws FilterException
    {
        List<EntityReference> newSpaces = this.currentSpaceReference.getReversedReferenceChain();
        List<EntityReference> previousSpaces = this.previousSpaceReference.getReversedReferenceChain();

        // Find the first different level
        int i = 0;
        while (i < previousSpaces.size() && i < newSpaces.size()) {
            if (!newSpaces.get(i).equals(previousSpaces.get(i))) {
                break;
            }

            ++i;
        }

        if (i < previousSpaces.size()) {
            // Delete what is different
            for (int diff = previousSpaces.size() - i; diff > 0; --diff, this.previousSpaceReference =
                this.previousSpaceReference.getParent()) {
                proxyFilter.endWikiSpace(this.previousSpaceReference.getName(), FilterEventParameters.EMPTY);
            }

            // If we got back to root forget about current space
            if (this.previousSpaceReference == null) {
                this.currentSpaceReference = null;
                this.sentBeginWikiSpace = false;
            }
        }
    }

    private boolean canSendBeginWikiDocument(boolean force)
    {
        return this.sentBeginWikiSpace
            && !this.sentBeginWikiDocument
            && (force || (this.currentDocumentReference != null
                && this.currentDocumentParameters.size() == XARDocumentModel.DOCUMENT_PARAMETERS.size() && this.properties
                .getEntities() == null));
    }

    private void sendBeginWikiDocument(XARInputFilter proxyFilter, boolean force) throws FilterException
    {
        sendBeginWikiSpace(proxyFilter, force);

        if (canSendBeginWikiDocument(force)) {
            sendBeginWikiSpace(proxyFilter, true);

            proxyFilter.beginWikiDocument(this.currentDocumentReference.getName(), this.currentDocumentParameters);
            this.sentBeginWikiDocument = true;
        }
    }

    private void sendEndWikiDocument(XARInputFilter proxyFilter) throws FilterException
    {
        sendBeginWikiDocument(proxyFilter, true);
        sendEndWikiDocumentLocale(proxyFilter);

        proxyFilter.endWikiDocument(this.currentDocumentReference.getName(), this.currentDocumentParameters);
        this.sentBeginWikiDocument = false;
    }

    private boolean canSendBeginWikiDocumentLocale(boolean force)
    {
        return this.sentBeginWikiDocument
            && !this.sentBeginWikiDocumentLocale
            && (force || (this.currentDocumentLocale != null && this.currentDocumentLocaleParameters.size() == XARDocumentModel.DOCUMENTLOCALE_PARAMETERS
                .size()));
    }

    private void sendBeginWikiDocumentLocale(XARInputFilter proxyFilter, boolean force) throws FilterException
    {
        if (force || (this.currentDocumentReference != null && this.currentDocumentLocale != null)) {
            LocalDocumentReference reference =
                new LocalDocumentReference(this.currentDocumentReference, this.currentDocumentLocale);

            if (this.properties.getEntities() != null && !this.properties.getEntities().matches(reference)) {
                throw new SkipEntityException(reference);
            }

            sendBeginWikiDocument(proxyFilter, force);

            if (canSendBeginWikiDocumentLocale(force)) {
                if (!this.properties.isWithHistory()) {
                    this.currentDocumentLocaleParameters.remove(XWikiWikiDocumentFilter.PARAMETER_JRCSREVISIONS);
                }

                proxyFilter.beginWikiDocumentLocale(this.currentDocumentLocale, this.currentDocumentLocaleParameters);
                this.sentBeginWikiDocumentLocale = true;
            }
        }
    }

    private void sendEndWikiDocumentLocale(XARInputFilter proxyFilter) throws FilterException
    {
        sendBeginWikiDocumentLocale(proxyFilter, true);
        sendEndWikiDocumentRevision(proxyFilter);

        proxyFilter.endWikiDocumentLocale(this.currentDocumentLocale, this.currentDocumentLocaleParameters);
        this.sentBeginWikiDocumentLocale = false;
    }

    private boolean canSendBeginWikiDocumentRevision(boolean force)
    {
        return this.sentBeginWikiDocumentLocale
            && !this.sentBeginWikiDocumentRevision
            && (force || (this.currentDocumentRevision != null && this.currentDocumentRevisionParameters.size() == XARDocumentModel.DOCUMENTREVISION_PARAMETERS
                .size()));
    }

    private void sendBeginWikiDocumentRevision(XARInputFilter proxyFilter, boolean force) throws FilterException
    {
        sendBeginWikiDocumentLocale(proxyFilter, force);

        if (canSendBeginWikiDocumentRevision(force)) {
            proxyFilter.beginWikiDocumentRevision(this.currentDocumentRevision, this.currentDocumentRevisionParameters);
            this.sentBeginWikiDocumentRevision = true;
        }
    }

    private void sendEndWikiDocumentRevision(XARInputFilter proxyFilter) throws FilterException
    {
        sendBeginWikiDocumentRevision(proxyFilter, true);

        proxyFilter.endWikiDocumentRevision(this.currentDocumentRevision, this.currentDocumentRevisionParameters);
        this.sentBeginWikiDocumentRevision = false;
    }

    public void read(Object filter, XARInputFilter proxyFilter) throws XMLStreamException, IOException, FilterException
    {
        XMLStreamReader xmlReader = XMLInputFilterStreamUtils.createXMLStreamReader(this.properties);

        try {
            read(xmlReader, filter, proxyFilter);
        } finally {
            this.properties.getSource().close();
        }
    }

    public void read(InputStream stream, Object filter, XARInputFilter proxyFilter) throws XMLStreamException,
        FilterException
    {
        XMLStreamReader xmlReader =
            this.properties.getEncoding() != null ? XMLInputFactory.newInstance().createXMLStreamReader(stream,
                this.properties.getEncoding()) : XMLInputFactory.newInstance().createXMLStreamReader(stream);

        read(xmlReader, filter, proxyFilter);
    }

    public void read(XMLStreamReader xmlReader, Object filter, XARInputFilter proxyFilter) throws XMLStreamException,
        FilterException
    {
        resetDocument();

        // <xwikidoc>

        xmlReader.nextTag();

        xmlReader.require(XMLStreamReader.START_ELEMENT, null, XarDocumentModel.ELEMENT_DOCUMENT);

        readDocument(xmlReader, filter, proxyFilter);
    }

    private void checkSpaces(XARInputFilter proxyFilter) throws FilterException
    {
        if (!this.currentSpaceReference.equals(this.previousSpaceReference)) {
            // Close spaces that needs to be closed
            if (this.sentBeginWikiSpace) {
                sendEndWikiSpace(proxyFilter);
            }
            // Open spaces that needs to be opened
            sendBeginWikiSpace(proxyFilter, false);
        }
    }

    private void readDocument(XMLStreamReader xmlReader, Object filter, XARInputFilter proxyFilter)
        throws XMLStreamException, FilterException
    {
        // Initialize with a few defaults (thing that don't exist in old XAR format)
        this.currentDocumentRevisionParameters.put(XWikiWikiDocumentFilter.PARAMETER_SYNTAX, Syntax.XWIKI_1_0);
        this.currentDocumentRevisionParameters.put(XWikiWikiDocumentFilter.PARAMETER_HIDDEN, false);

        // Reference
        String referenceString =
            xmlReader.getAttributeValue(XARDocumentModel.ATTRIBUTE_DOCUMENT_REFERENCE,
                XARDocumentModel.ATTRIBUTE_DOCUMENT_REFERENCE);
        if (StringUtils.isNotEmpty(referenceString)) {
            this.currentDocumentReference = this.relativeResolver.resolve(referenceString, EntityType.DOCUMENT);

            // Remember previous space reference
            this.previousSpaceReference = this.currentSpaceReference;
            // Get new space reference
            this.currentSpaceReference = this.currentDocumentReference.getParent();

            checkSpaces(proxyFilter);
        }

        // Locale
        String localeString =
            xmlReader.getAttributeValue(XARDocumentModel.ATTRIBUTE_DOCUMENT_LOCALE,
                XARDocumentModel.ATTRIBUTE_DOCUMENT_LOCALE);
        if (localeString != null) {
            this.currentDocumentLocale = toLocale(localeString);
            this.localeFromLegacy = false;
        }

        for (xmlReader.nextTag(); xmlReader.isStartElement(); xmlReader.nextTag()) {
            String elementName = xmlReader.getLocalName();

            if (elementName.equals(XARAttachmentModel.ELEMENT_ATTACHMENT)) {
                readAttachment(xmlReader, filter, proxyFilter);
            } else if (elementName.equals(XARObjectModel.ELEMENT_OBJECT)) {
                readObject(xmlReader, filter, proxyFilter);
            } else if (elementName.equals(XARClassModel.ELEMENT_CLASS)) {
                readClass(xmlReader, filter, proxyFilter);
            } else {
                String value = xmlReader.getElementText();

                if (XarDocumentModel.ELEMENT_SPACE.equals(elementName)) {
                    this.currentLegacySpace = value;

                    if (this.currentDocumentReference == null) {
                        // Its an old thing
                        this.previousSpaceReference = this.currentSpaceReference;
                        if (this.currentLegacyDocument == null) {
                            this.currentSpaceReference = new EntityReference(value, EntityType.SPACE);
                        } else {
                            this.currentDocumentReference =
                                new LocalDocumentReference(this.currentLegacySpace, this.currentLegacyDocument);
                            this.currentSpaceReference = this.currentDocumentReference.getParent();
                        }

                        checkSpaces(proxyFilter);
                    }
                } else if (XarDocumentModel.ELEMENT_NAME.equals(elementName)) {
                    this.currentLegacyDocument = value;

                    if (this.currentDocumentReference == null) {
                        // Its an old thing
                        if (this.currentLegacySpace != null) {
                            this.currentDocumentReference =
                                new LocalDocumentReference(this.currentLegacySpace, this.currentLegacyDocument);
                            this.currentSpaceReference = this.currentDocumentReference.getParent();
                        }
                    }
                } else if (XarDocumentModel.ELEMENT_LOCALE.equals(elementName)) {
                    if (this.localeFromLegacy) {
                        this.currentDocumentLocale = toLocale(value);
                    }
                } else if (XarDocumentModel.ELEMENT_REVISION.equals(elementName)) {
                    this.currentDocumentRevision = value;
                } else {
                    EventParameter parameter = XARDocumentModel.DOCUMENT_PARAMETERS.get(elementName);

                    if (parameter != null) {
                        Object wsValue = convert(parameter.type, value);
                        if (wsValue != null) {
                            this.currentDocumentParameters.put(parameter.name, wsValue);
                        }
                    } else {
                        parameter = XARDocumentModel.DOCUMENTLOCALE_PARAMETERS.get(elementName);

                        if (parameter != null) {
                            Object wsValue = convert(parameter.type, value);
                            if (wsValue != null) {
                                this.currentDocumentLocaleParameters.put(parameter.name, wsValue);
                            }
                        } else {
                            parameter = XARDocumentModel.DOCUMENTREVISION_PARAMETERS.get(elementName);

                            if (parameter != null) {
                                Object objectValue;
                                if (parameter.type == EntityReference.class) {
                                    objectValue = this.relativeResolver.resolve(value, EntityType.DOCUMENT);
                                } else {
                                    objectValue = convert(parameter.type, value);
                                }

                                if (objectValue != null) {
                                    this.currentDocumentRevisionParameters.put(parameter.name, objectValue);
                                }
                            } else {
                                // Unknown property
                                // TODO: log something ?
                            }
                        }
                    }
                }
            }
        }

        sendBeginWikiDocumentRevision(proxyFilter, true);
        sendWikiAttachments(proxyFilter);
        sendWikiClass(proxyFilter);
        sendWikiObjects(proxyFilter);
        sendEndWikiDocument(proxyFilter);
    }

    private void readObject(XMLStreamReader xmlReader, Object filter, XARInputFilter proxyFilter)
        throws XMLStreamException, FilterException
    {
        sendBeginWikiDocumentRevision(proxyFilter, false);

        WikiObject wikiObject = this.objectReader.read(xmlReader);

        if (this.sentBeginWikiDocumentRevision) {
            wikiObject.send(proxyFilter);
        } else {
            this.currentObjects.offer(wikiObject);
        }
    }

    private void readClass(XMLStreamReader xmlReader, Object filter, XARInputFilter proxyFilter)
        throws XMLStreamException, FilterException
    {
        sendBeginWikiDocumentRevision(proxyFilter, false);

        this.currentClass = this.classReader.read(xmlReader);

        if (this.sentBeginWikiDocumentRevision) {
            sendWikiClass(proxyFilter);
        }
    }

    private void readAttachment(XMLStreamReader xmlReader, Object filter, XARInputFilter proxyFilter)
        throws XMLStreamException, FilterException
    {
        sendBeginWikiDocumentRevision(proxyFilter, false);

        WikiAttachment wikiAttachment = this.attachmentReader.read(xmlReader);

        if (this.sentBeginWikiDocumentRevision) {
            wikiAttachment.send(proxyFilter);
        } else {
            this.currentAttachments.offer(wikiAttachment);
        }
    }

    private void sendWikiClass(XARInputFilter proxyFilter) throws FilterException
    {
        if (this.currentClass != null && !this.currentClass.isEmpty()) {
            this.currentClass.send(proxyFilter);
            this.currentClass = null;
        }
    }

    private void sendWikiObjects(XARInputFilter proxyFilter) throws FilterException
    {
        while (this.currentObjects.size() > 0) {
            this.currentObjects.poll().send(proxyFilter);
        }
    }

    private void sendWikiAttachments(XARInputFilter proxyFilter) throws FilterException
    {
        while (this.currentAttachments.size() > 0) {
            this.currentAttachments.poll().send(proxyFilter);
        }
    }
}
