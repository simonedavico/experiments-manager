package cloud.benchflow.experimentsmanager.db.generators;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import cloud.benchflow.experimentsmanager.db.entities.Experiment;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 03/02/16.
 */
public class ExperimentNumberGenerator implements IdentifierGenerator {

    @Override
    public synchronized Serializable generate(SessionImplementor session, Object o) throws HibernateException {

        Connection c = session.connection();
        Experiment exp = (Experiment) o;
        String benchmarkName = exp.getBenchmarkName();
        String username = exp.getUsername();
        try {

            PreparedStatement ps = c.prepareStatement("SELECT MAX(EXP_NUMBER)+1 FROM EXPERIMENTS WHERE USERNAME = '" + username
                                                    + "' AND BENCHMARK_NAME = '" + benchmarkName + "'");
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                long count = rs.getLong("MAX(EXP_NUMBER)+1");
                return count;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new HibernateException("Error while retrieving count of experiments for user " + username +
                                         "and benchmark " + benchmarkName);
        }
        return null;
    }
}
