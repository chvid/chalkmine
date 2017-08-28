package com.apelab.chalkmine;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created on 28/08/2017.
 *
 * @author Christian Hvid
 */
public interface Mapper<T> {
    T map(ResultSet rs) throws SQLException;
}
