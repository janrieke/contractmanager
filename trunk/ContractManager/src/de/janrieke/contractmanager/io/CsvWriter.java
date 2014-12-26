/*
 *   This file is part of ContractManager for Jameica.
 *   Copyright (C) 2010-2015  Jan Rieke
 *
 *   ContractManager is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   ContractManager is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *   
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.janrieke.contractmanager.io;

import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.serialize.AbstractXmlIO;
import de.willuhn.datasource.serialize.Writer;
import de.willuhn.logging.Logger;

/**
 * Comma-separated-values serializer
 */
public class CsvWriter extends AbstractXmlIO implements Writer {
	private final static String ENCODING = "UTF-8";
	private final static String SEPARATOR = ";";
	
	private final static String END_OF_LINE = "\r\n";
	
	private ZipOutputStream zos = null;
	private Class<?> currentType = null;
	private String[] currentAttributes;

	public CsvWriter(OutputStream os) {
		this.zos = new ZipOutputStream(os) {
			@Override
			public void close() throws IOException {
				Logger.debug("csv generation complete, closing csv zip file");
				super.close();
			}
		};
	}

	/**
	 * @see de.willuhn.datasource.serialize.IO#close()
	 */
	public void close() throws IOException {
		this.zos.close();
	}

	/**
	 * Returns the field names of attributes that should be serialized.
	 * Can be overridden by subclasses. This default implementation simply
	 * uses {@link de.willuhn.datasource.GenericObject#getAttributeNames()}.
	 * 
	 * Notice: Always return the same set of attributes for each instance of
	 * a class. Otherwise the attribute names in the first row may not fit all
	 * entries.
	 * 
	 * @param object
	 *            the object to be serialized
	 * @return attributes to be serialized.
	 * @throws RemoteException
	 */
	public String[] getAttributeNames(GenericObject object) throws RemoteException {
		return object.getAttributeNames();
	}

	/**
	 * @see de.willuhn.datasource.serialize.Writer#write(de.willuhn.datasource.GenericObject)
	 */
	public synchronized void write(GenericObject object) throws IOException {
		if (!object.getClass().equals(currentType)) {
			// If this is the first instance of a class, create a new csv file within the zip file.
			Logger.debug("creating csv file for " + currentType);
			currentType = object.getClass();
			StringBuilder sb = new StringBuilder(currentType.getSimpleName());
			if (sb.length()>4 && "Impl".equals(sb.substring(sb.length()-4)))
				sb.delete(sb.length()-4, sb.length());
			sb.append(".csv");
			zos.putNextEntry(new ZipEntry(sb.toString()));

			// Write attribute names to the first row.
			currentAttributes = getAttributeNames(object);
			sb = new StringBuilder();
			sb.append(encode("id"));
			for (int i = 0; i < currentAttributes.length; ++i) {
				sb.append(SEPARATOR);
				sb.append(encode(currentAttributes[i]));
			}
			sb.append(END_OF_LINE);
			zos.write(sb.toString().getBytes(ENCODING));
		}
		
		// Perform serialization
		Logger.debug("serializing object: " + object.getClass().getName() + ":" + object.getID());
		StringBuffer sb = new StringBuffer();
		sb.append(encode(object.getID()));
		for (int i = 0; i < currentAttributes.length; ++i) {
			Object o = object.getAttribute(currentAttributes[i]);
			if (o == null)
				o = "";

			String type = o.getClass().getName();
			if (o instanceof GenericObject) {
				type = Integer.class.getName();
				o = ((GenericObject) o).getID();
			}

			Value v = (Value) valueMap.get(type);
			if (v == null)
				v = (Value) valueMap.get(null);

			sb.append(SEPARATOR);
			sb.append(encode(v.serialize(o)));
		}
		sb.append(END_OF_LINE);
		zos.write(sb.toString().getBytes(ENCODING));
	}

	/**
	 * Performs CSV escaping.
	 * 
	 * @param s
	 *            the String to escape.
	 * @return the escaped String.
	 */
	private String encode(String s) {
		s = s.replace("\"", "\"\"");
		s = s.replace("\r\n", "\n");
		return "\"" + s + "\"";
	}
}