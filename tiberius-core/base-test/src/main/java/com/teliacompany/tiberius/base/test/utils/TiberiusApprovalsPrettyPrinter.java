package com.teliacompany.tiberius.base.test.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import java.io.IOException;

public class TiberiusApprovalsPrettyPrinter extends DefaultPrettyPrinter {

    public TiberiusApprovalsPrettyPrinter() {
        DefaultPrettyPrinter.Indenter indenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
        indentObjectsWith(indenter);
        indentArraysWith(indenter);
        _objectFieldValueSeparatorWithSpaces = ": ";
    }

    private TiberiusApprovalsPrettyPrinter(TiberiusApprovalsPrettyPrinter pp) {
        super(pp);
    }

    @Override
    public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
        if (!_arrayIndenter.isInline()) {
            --_nesting;
        }
        if (nrOfValues > 0) {
            _arrayIndenter.writeIndentation(g, _nesting);
        }
        g.writeRaw(']');
    }

    @Override
    public void writeEndObject(JsonGenerator g, int nrOfEntries) throws IOException
    {
        if (!_objectIndenter.isInline()) {
            --_nesting;
        }
        if (nrOfEntries > 0) {
            _objectIndenter.writeIndentation(g, _nesting);
        }
        g.writeRaw('}');
    }

    @Override
    public DefaultPrettyPrinter createInstance() {
        return new TiberiusApprovalsPrettyPrinter(this);
    }

}
