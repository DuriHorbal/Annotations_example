package sk.tuke.juho.generated;

import java.util.*;

public class TableMaker {

	public static ArrayList<String> getQueriesList() {
		ArrayList<String> queries = new ArrayList<>();

		String Pracovisko_seq ="CREATE SEQUENCE Pracovisko_seq START 1;";
		queries.add(Pracovisko_seq);
		String zamestnanec_seq ="CREATE SEQUENCE zamestnanec_seq START 1;";
		queries.add(zamestnanec_seq);
		String tablezamestnanec="CREATE TABLE zamestnanec ( id_zamestnanca integer PRIMARY KEY DEFAULT nextval('zamestnanec_seq'), meno varchar(50) NOT NULL, vek integer, plat float NOT NULL, pracovisko_id integer);";
		queries.add(tablezamestnanec);
		String tablePracovisko="CREATE TABLE Pracovisko ( idDepartmentu integer PRIMARY KEY DEFAULT nextval('Pracovisko_seq'), meno varchar(50) NOT NULL, kod varchar(20), sef_id integer);";
		queries.add(tablePracovisko);
		String alterzamestnanec ="ALTER TABLE zamestnanec  ADD FOREIGN KEY (pracovisko_id) REFERENCES Pracovisko (idDepartmentu)";
		queries.add(alterzamestnanec);
		String alterPracovisko ="ALTER TABLE Pracovisko  ADD FOREIGN KEY (sef_id) REFERENCES zamestnanec (id_zamestnanca)";
		queries.add(alterPracovisko);

		return queries;
	}
}