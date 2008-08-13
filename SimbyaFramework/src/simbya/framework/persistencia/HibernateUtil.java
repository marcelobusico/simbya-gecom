/*
 * Copyright (C) 2008  Marcelo Busico <marcelobusico@simbya.com.ar>
 * 
 * This file is part of a SIMBYA project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package simbya.framework.persistencia;

import java.util.Properties;
import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.cfg.*;

public class HibernateUtil {

    private static final Logger log = Logger.getLogger(HibernateUtil.class);
    private static final SessionFactory sessionFactory;
    

    static {
        try {
            //Crea SessionFactory usando las propiedades de hibernate.cfg.xml
            Properties p = new Properties();
            Configuration conf = new Configuration();
            conf.setProperties(p);
            sessionFactory = conf.configure().buildSessionFactory();
        } catch (Throwable ex) {
            log.error("Initial SessionFactory creation failed", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Obtiene la fábrica de sesiones de Hibernate para trabajar con la base de datos.
     * @return Fábrica de Sesiones de Hibernate.
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
