/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2016 GAEL Systems
 *
 * This file is part of DHuS software sources.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.gael.dhus.database.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.KeyStoreEntry;
import fr.gael.dhus.database.object.KeyStoreEntry.Key;
import fr.gael.dhus.util.CheckIterator;
import fr.gael.dhus.util.TestContextLoader;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import org.testng.annotations.Test;

/*
 * used from spring-test v 3.2.2 (current version on DHuS 3.2.1)
 * @WebAppConfiguration
 * @ContextHierarchy(
 * @ContextConfiguration(locations = "classpath:spring/context-test.xml"))
 */
@ContextConfiguration(locations = "classpath:fr/gael/dhus/spring/context-test.xml",
      loader = TestContextLoader.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestKeyStoreEntryDao extends TestAbstractHibernateDao<KeyStoreEntry, Key>
{
   @Autowired
   private KeyStoreEntryDao dao;

   @Override
   protected HibernateDao<KeyStoreEntry, Key> getHibernateDao()
   {
      return dao;
   }

   @Override
   protected int howMany()
   {
      return 6;
   }

   @Test(dependsOnMethods = {"create"}, expectedExceptions = {DuplicateKeyException.class})
   public void primaryKey()
   {
      KeyStoreEntry kse = new KeyStoreEntry("Toto", "key", "1");
      dao.create(kse);
      assertEquals(dao.count(), 7);
      KeyStoreEntry kse2 = new KeyStoreEntry("Toto", "key", "2");
      dao.create(kse2);
   }

   @Override
   @Test
   public void create()
   {
      KeyStoreEntry kse = new KeyStoreEntry("Toto", "key", "1");
      dao.create(kse);
      assertEquals(dao.count(), 7);
   }

   @Override
   @Test
   public void read()
   {
      Key key = new Key("store1", "key1");
      assertEquals(dao.read(key).getValue(), "value1");
   }

   @Override
   @Test(dependsOnMethods = {"read"})
   public void update()
   {
      Key nk = new Key("store2", "key1");
      KeyStoreEntry kse = dao.read(nk);
      assertEquals(kse.getValue(), "value1");
      kse.setValue("toto");
      dao.update(kse);
      assertEquals(dao.read(nk).getValue(), "toto");
   }

   @Override
   @Test(dependsOnMethods = {"read"})
   public void delete()
   {
      Key nk = new Key("store2", "key1");
      KeyStoreEntry kse = dao.read(nk);
      assertEquals(dao.count(), 6);
      dao.delete(kse);
      assertEquals(dao.count(), 5);
   }

   @Override
   public void scroll()
   {
      String hql = "WHERE KEYSTORE = 'store3'";
      Iterator<KeyStoreEntry> it = dao.scroll(hql, -1, -1).iterator();
      assertTrue(CheckIterator.checkElementNumber(it, 2));
   }

   @Override
   public void first()
   {
      String hql = "FROM KeyStoreEntry ORDER BY keyStore, entryKey";
      KeyStoreEntry kse = dao.first(hql);

      assertNotNull(kse);
      assertEquals(kse.getValue(), "value1");
   }
}
