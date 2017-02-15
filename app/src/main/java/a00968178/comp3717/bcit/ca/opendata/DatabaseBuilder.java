package a00968178.comp3717.bcit.ca.opendata;


import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;


import a00968178.comp3717.bcit.ca.opendata.databases.OpenHelper;


/**
 * Created by Brayden on 2/6/2017.
 */

public final class DatabaseBuilder {


    private static final String TAG = DatabaseBuilder.class.getName();

    private static final String OPENDATA_DOMAIN = "opendata.newwestcity.ca";
    private static final String CATEGORIES_URL = "http://"+OPENDATA_DOMAIN+"/categories";
    private static final String CATEGORIES_SELECT = "body .container-fluid > .nav li > a";

    private static final String CATEGORY_NAME_SELECT = "body h1";
    private static final String DATASETS_SELECT = "body .container-fluid h3 > a";
    private static final String DATASETS_DESC_SELECT = "body > .container-fluid > p";

    private static final int MAX_ERROR_TOAST = 3; // max error pages to show on Toast.




    private OpenHelper categoriesHelper;
    private OpenHelper datasetsHelper;

    private SQLiteDatabase catDB;
    private SQLiteDatabase dsDB;

    public DatabaseBuilder(Context context) {
        categoriesHelper = new CategoriesOpenHelper(context);
        datasetsHelper = new DatasetsOpenHelper(context);

        catDB = categoriesHelper.getWritableDatabase();
        dsDB  = datasetsHelper.getWritableDatabase();
    }


    private void insertCategory(int id, String name, int count) {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("_id", ""+id);
        hm.put("category_name", name);
        hm.put("dataset_count", ""+count);
        categoriesHelper.insert(catDB, hm);
    }

    private void insertDataset(int id, int category_id, String name, String desc, String link) {

        if(desc.trim().equals("")) desc = "No Description Available";

        HashMap<String, String> hm = new HashMap<String, String>();
        //hm.put("_id", ""+id);
        hm.put("category_id", ""+category_id);
        hm.put("dataset_name", name);
        hm.put("dataset_desc", desc);
        hm.put("dataset_link", link);
        datasetsHelper.insert(dsDB, hm);
    }

    public void cleanup() {

        categoriesHelper.rebuildTable();
        datasetsHelper.rebuildTable();
    }

    public void populateCategories() {
        final SQLiteDatabase db;
        final long           numEntries;

        OpenHelper helper = categoriesHelper;

        db = helper.getWritableDatabase();
        numEntries = helper.getNumberOfRows();

        if(numEntries == 0)
        {
            db.beginTransaction();

            try
            {
                int i = 0;
                insertCategory(++i, "Business and Economy Datasets" ,12);
                insertCategory(++i, "City Government Datasets", 7);
                insertCategory(++i, "Community Datasets", 6);
                insertCategory(++i, "Electrical Datasets", 1);
                insertCategory(++i, "Environment Datasets", 1);
                insertCategory(++i, "Finance Datasets", 3);
                insertCategory(++i, "Heritage Datasets", 3);
                insertCategory(++i, "Lands and Development Datasets", 14);
                insertCategory(++i, "Parks and Recreation Datasets", 11);
                insertCategory(++i, "Public Safety Datasets", 8);
                insertCategory(++i, "Transportation Datasets", 17);
                insertCategory(++i, "Utilities Datasets", 10);

                db.setTransactionSuccessful();
            }
            finally
            {
                db.endTransaction();
            }
        }

        db.close();
    }


    private int getNumberOfDatasets(String category_name) {

        Cursor cursor = categoriesHelper.getRow(null, "category_name", category_name);

        cursor.moveToFirst();
        int number = cursor.getInt(cursor.getColumnIndex("dataset_count"));
        cursor.close();
        return number;
    }

// So we can end db transactions.
    public int sync() throws IOException, NoChangeException {

        int result = -1;

        categoriesHelper.getWritableDatabase().beginTransactionNonExclusive();
        datasetsHelper.getWritableDatabase().beginTransactionNonExclusive();

        try {

            //cleanup(); // delete current data
            //categoriesHelper.rebuildTable(); // delete current data
            result = syncHandler();
            categoriesHelper.getWritableDatabase().setTransactionSuccessful();
            datasetsHelper.getWritableDatabase().setTransactionSuccessful();
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            categoriesHelper.getWritableDatabase().endTransaction();
            datasetsHelper.getWritableDatabase().endTransaction();
        }
        return result;
    }

    private int syncHandler() throws IOException, NoChangeException {
        //InputStream categories = httpInputStream(CATEGORIES_URL);

        int originalCategoryCount = (int)categoriesHelper.getNumberOfRows();
        int originalDatasetCount   = (int)datasetsHelper.getNumberOfRows();

        Log.d(TAG, "categories: "+originalCategoryCount+" datasets: "+originalDatasetCount);

        int categoryCount = 0;
        int datasetCount = 0;


        ArrayList<String> failedCategories = new ArrayList<String>();
        ArrayList<String> failedDatasets = new ArrayList<String>();

        try {
            Elements categories = Jsoup.connect(CATEGORIES_URL).get().select(CATEGORIES_SELECT);
            for (Element category : categories) {
                Log.d(TAG, "Category "+(categoryCount+1));

                String link = category.attr("href");

                try {
                    Document categoryDoc = Jsoup.connect(link).get();

                    String name = categoryDoc.select(CATEGORY_NAME_SELECT).first().text().trim();
                    if(name.equals("")) throw new IOException("Category "+categoryCount+" has no name!");


                    Elements datasets = categoryDoc.select(DATASETS_SELECT);

                    Cursor c = categoriesHelper.getRow(null, "category_name", name);
                    //c.moveToFirst();
                    int datasetsIndex = c.getColumnIndex("dataset_count");
                    Log.d(TAG, "datasets index: "+datasetsIndex);

                    int numDatasets;
                    Log.d(TAG, "category " + name + " columns: " + c.getColumnCount());
                    try {
                        numDatasets = c.getInt(datasetsIndex);
                    } catch (CursorIndexOutOfBoundsException e) {
                        Log.e(TAG, "c.getInt("+datasetsIndex+") exception: "+e.getMessage());
                        numDatasets = 0;
                    }
                        //int numDatasets = 0;

                    Log.d(TAG, "checking category '"+name+"' datasets: "+numDatasets+" and on web: "+datasets.size());
                    if(numDatasets == datasets.size()) {
                        Log.d(TAG, "No change for category "+name);
                        continue;
                    }



                    try {
                        int existCatId  = categoriesHelper.getId("category_name = ?", new String[] {name} );
                        categoriesHelper.delete(existCatId);
                        datasetsHelper.delete("category_id = ?", new String[] {""+existCatId}); // delete where category_id = this
                        insertCategory(++categoryCount, name, datasets.size());

                    } catch (Resources.NotFoundException e) {
                        Log.w(TAG,"Unable to get category ID for "+name);
                    }

                    for (Element dataset : datasets) {
                        Log.d(TAG, "Dataset "+(datasetCount+1));

                        String link2 = dataset.attr("href");

                        try {
                            Document datasetDoc = Jsoup.connect(link2).get();
                            String datasetName = dataset.text();
                            String datasetDesc = datasetDoc.select(DATASETS_DESC_SELECT).text();

                            insertDataset(++datasetCount, categoryCount, datasetName, datasetDesc, link2);

                        } catch (IOException e) {
                            failedDatasets.add(dataset.text());
                        }



                    }
                } catch (IOException e) {
                    failedCategories.add(category.text().replaceAll("[0-9]", ""));
                }


            }
        } catch (IOException e) {
            throw new IOException("Unable to connect to "+OPENDATA_DOMAIN);
        }


        if(failedCategories.size() > 0) {
            String failed = "Failed ot load ("+failedCategories.size()+") Categories: ";

            int i;
            for(i = 0; i <= MAX_ERROR_TOAST && i < failedCategories.size(); i++) {
                failed += failedCategories.get(i);
                if(i+1 != MAX_ERROR_TOAST && i+1 < failedCategories.size()) failed += ", ";
            }
            if(i < failedCategories.size()) failed += "...";

            throw new IOException(failed);

        } else if (failedDatasets.size() > 0) {
            String failed = "Failed ot load ("+failedDatasets.size()+") Datasets: ";

            int i;
            for(i = 0; i <= MAX_ERROR_TOAST && i < failedDatasets.size(); i++) {
                failed += failedDatasets.get(i);
                if(i+1 != MAX_ERROR_TOAST && i+1 < failedDatasets.size()) failed += ", ";
            }
            if(i < failedDatasets.size()) failed += "...";

            throw new IOException(failed);
        }

        Log.d(TAG, "orig dataset: "+originalDatasetCount+" now datasets: "+datasetCount);


        datasetCount = (int)datasetsHelper.getNumberOfRows();

        if(datasetCount == originalDatasetCount) throw new NoChangeException("Already up-to-date");
        if(datasetCount < originalDatasetCount) throw new IOException("Failed to update DB...");

        return datasetCount - originalDatasetCount;
    }



    public int populateDatasets() {
        final SQLiteDatabase db;
        final long           numEntries;

        int i = 0;

        OpenHelper helper = datasetsHelper;

        db = helper.getWritableDatabase();
        numEntries = helper.getNumberOfRows();

        if(numEntries == 0)
        {
            db.beginTransaction();

            try
            {
                i = 0;
                //insertDataset(++i, 0, "get = function(elem) { xml += "" + elem.text() + "\n" }", "", "");
                insertDataset(++i, 1, "Business Licenses (Active - Resident)", "\nNew Westminster has an annual renewal of approximately 4,000 business licenses each year. Business Licensing also issues licenses for liquor establishments and municipal decals.\nThings to know\n1) Before you sign a lease, it’s important for prospective business owners who are applying for business licenses to check with the Building, Planning and Licensing divisions on property they wish to lease or buy in regards to outstanding orders or issues pertaining to that property.\n2) Before you sign a lease, check with the Planning and Building Department to make sure your business is a permitted use on the site.\n3) Before you purchase a sign for your business, review the requirements of the sign bylaw with the Planning Division. Click here for Sign Permit information.\n4) Each space in a building has its own specific approved use and sometimes the use of that space cannot be changed without approval and/or permit.\nhttps://www.newwestcity.ca/business_licences.php\n\n\n			CSV (574 KB) | XLSX (4 MB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/business-license-active-resident");
                insertDataset(++i, 1, "Business Licenses (Inter-Municipal)", "\nAs of October 1, 2013, an Inter-municipal Business License will be available in the Metro West region. For $250, eligible businesses may be licensed to work in all of the following municipalities: \nCity of New Westminster\nCity of Burnaby\nCorporation of Delta\nCity of Richmond\nCity of Surrey\nCity of Vancouver\nEligibility is limited to inter-municipal businesses, defined as trades contractors or other professionals (related to the construction industry) that provide a service or product other than from their fixed and permanent location. Only eligible businesses which have fixed and permanent location in one of the participating municipalities are eligible for the IMBL.\nFor further information, please contact the City of New Westminster Business Licensing Office at 604-527-4565.\nhttps://www.newwestcity.ca/business_licences.php\n\n\n			CSV (25 KB) | XLSX (13 KB)\n	\n\n", "");
                insertDataset(++i, 1, "Business Licenses (New for 2016)", "\nEvery business in the City of New Westminster is required to have a valid business license before beginning operation. This includes home-based businesses, commercial and industrial operations and owners of apartment rental properties.\nhttp://www.newwestcity.ca/business/permits_licenses/business_licences.php\n\n\n			CSV (121 KB) | XLSX (53 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/business-licenses-approved-2016");
                insertDataset(++i, 1, "Business Licenses (Non-Residents)", "\nContractors from different municipalities doing business within New Westminster\nhttps://www.newwestcity.ca/business_licences.php\n\n\n			CSV (118 KB) | XLSX (42 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/business-licenses-non-residents");
                insertDataset(++i, 1, "Demographic - Detailed Age Profile (Census 2011)", "\nCensus 2011 information summarized by ages (by individual years of age and age groupings) and gender,  neighborhoods, census tracts and dissemination areas. Also contains descriptive information about the data source files and notes about the use of the data.\n\n\n			XLSX (234 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/2011-census-detailed-age-profile-by-neighbourhood");
                insertDataset(++i, 1, "Demographic Profiles (Census 1986,1991,1996,2001,2006)", "\nInformation for the City of New Westminster from the 1986, 1991, 1996, 2001 and 2006 Censuses.\nThis information includes age, housing characteristics, immigration, ethnicity, labour force, population change, income, education, household type, language information etc.  Also contains descriptive information about the data source files and notes about the use of the data.\n\n\n			XLSX (180 KB)\n	\n\n", "");
                insertDataset(++i, 1, "Demographic Profiles (Census 2011)", "\n2011 census information summarized by \na) city\nb) neighborhoods\nc) census tracts and\nd) dissemination areas.  \nCensus information includes age, household type, family type, mother tongue, home language, type of dwelling etc. Also contains descriptive information about the data source files and notes about the use of the data.\n\n\n			XLSX (269 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/2011-census-profiles-byneighbourhood");
                insertDataset(++i, 1, "Demographic Profiles (National Household Survey 2011)", "\nContains tabs with 2011 National Household Survey information for the City of New Westminster, New Westminster neighbourhoods and New Westminster census tracts. National Household Survey includes information on income, housing characteristics, ethnicity, immigration, education, labour force etc. Also contains descriptive information about the data source files and notes about the use of the data.\n\n\n			XLSX (215 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/national-household-survey-by-neighbourhood-2011");
                insertDataset(++i, 1, "Sidewalk Café Location (Sidewalk Encroachment Agreements)", "\nList of the locations of all Sidewalk Encroachment Agreements\n\n\n			XLS (30 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/sidewalk-cafe-location");
                insertDataset(++i, 1, "Workforce - New West Resident Commuting Patterns (NHS 2011)", "\nThis file contains information on the commuting patterns of workers who live in New Westminster (regardless of which municipality their place of work is located in). The information is from the 2011 National Household Survey and contains mode of transportation, time leaving for work, commute duration and commuting destinations. Also contains descriptive information about the data source files and notes about the use of the data.\n\n\n			XLSX (24 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/2011-national-household-survey-commuting-patterns-");
                insertDataset(++i, 1, "Workforce - New West Worker Commuting Patterns (NHS 2011)", "\nContains information on workers who work within the boundaries of the City of New Westminster (regardless of their municipality of residence).\nInformation is from the 2011 National Household Survey and includes mode of transportation, time arriving at work, commute duration and commuting origin. Also contains descriptive information about the data source files and notes about the use of the data.\n\n\n			XLSX (24 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/2011nhs-commuting-patterns-workers-who-work-in-new");
                insertDataset(++i, 1, "Workforce Profile (NHS 2011)", "\n2011 National Household Survey information on workers who work in New Westminster (regardless of municipality of residence).\nInformation includes occupation, industry, employment income (before-tax), work activity, age and sex and education. Also contains descriptive information about the data source files and notes about the use of the data. \n\n\n			XLSX (30 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/2011-national-household-survey-workers-who-work-in");
                insertDataset(++i, 2, "Addresses", "\nA list of addresses for the City of New Westminster.\n\n\n			CSV (3 MB) | KMZ (2 MB) | SHP (895 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/address-points");
                insertDataset(++i, 2, "City Boundaries", "City of New Westminster Boundaries.\n\n", "http://opendata.newwestcity.ca/datasets/city-boundaries");
                insertDataset(++i, 2, "City Owned Property", "Parcels of property currently owned by the Corporation of the City of New Westminster.\n\n", "http://opendata.newwestcity.ca/datasets/city-owned-property");
                insertDataset(++i, 2, "Councillor Contact Information", "\nThe City of New Westminster wants to facilitate residents and the general public access to the elected officials of the City.  \n\n\n			CSV (756 B) | XLSX (11 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/councillor-contact-information");
                insertDataset(++i, 2, "Election Results 1990 - Present", "\nThe spreadsheet provides the candidates, voting locations and the results for the candidate both total and by location for each election from 1990 forward.\n\n\n			XLSX (12 KB) | XLSX (62 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/electrion-results-1990-present");
                insertDataset(++i, 2, "Neighbourhoods Boundaries", "These new boundaries are used for contemporary planning functions and are the basis for most of the statistics used in recent Census data published by the City of New Westminster…\n\n", "http://opendata.newwestcity.ca/datasets/neighbourhoods");
                insertDataset(++i, 2, "Number of City Employees", "\nFinancial Disclosure form completed annual by all elected officials. Number of city employees by year.\n\n\n			XLSX (11 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/number-of-city-employees");
                insertDataset(++i, 3, "Cemeteries", "\n\n", "http://opendata.newwestcity.ca/datasets/cemeteries");
                insertDataset(++i, 3, "City Facility Sites", "\n\n", "http://opendata.newwestcity.ca/datasets/city-facility-sites");
                insertDataset(++i, 3, "Community Service Assets", "A listing of community services and supports. More specifically, it includes information on emergency, transitional and supportive housing; transition and second stage housing for…\n\n", "http://opendata.newwestcity.ca/datasets/community-service-assets");
                insertDataset(++i, 3, "School Buildings", "\n\n", "http://opendata.newwestcity.ca/datasets/significant-buildings-schools");
                insertDataset(++i, 3, "School Catchment Boundaries", "Primary, Middle and Secondary School Boundaries.\n\n", "http://opendata.newwestcity.ca/datasets/school-catchment-boundaries");
                insertDataset(++i, 3, "School Sites", "\n\n", "http://opendata.newwestcity.ca/datasets/school-sites");
                insertDataset(++i, 4, "City Energy Use Through Time", "\nFinancial disclosure form completed annually by all elected officials. Shows the amount of energy consumed and greenhouse gases created through time.\n\n\n			XLSX (204 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/city-energy-use-through-time");
                insertDataset(++i, 5, "Riparian", "\n\n", "http://opendata.newwestcity.ca/datasets/riparian");
                insertDataset(++i, 6, "Grants (Awarded for 2016)", "\nA spreadsheet and accompanying documents listing grants awarded for year 2016.\nGrant Categories;\n1) Festival Event Grants 2) Heritage Grants 3) Environmental Grants 4) Community Grants 5) Arts and Culture Grants 6) Child Care Grants 6) City Partnership Grants 7) Amateur Sports Grants\nFor more information go to City Grants Page\nSupporting documents;\nCity Grants Summary Sheet\n2016 City Partnership Grants.zip\n2016 Festival Grants.zip\n2016 Community Grants.zip\n2016 Arts and Culture Grants.zip\n2016 Amateur Sport Grants.zip\n2016 Heritage Grants.zip\n2016 Child Care Grant Program.zip\n2016 Environmental Grants.zip\n\n\n			No Downloads Available\n	\n\n", "http://opendata.newwestcity.ca/datasets/grants-approved-for-2016");
                insertDataset(++i, 6, "Schedule of Goods and Services (2015)", "\nExcel listing of all suppliers and service provides in the report period, plus the amount paid\n\n\n			CSV (11 KB) | XLSX (21 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/schedule-of-goods-and-services");
                insertDataset(++i, 6, "Statement of Financial Information (2015)", "\nRemuneration of City Employees &amp; Council Members\n\n\n			XLSX (30 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/statement-of-financial-information");
                insertDataset(++i, 7, "Building Age", "\nThe age of most buildings in the City (year it was built) as well as some historical data such as the Building Name, Developer/Builder, Architect/Designer and year the building has been moved if relevant and available. \n\n\n			CSV (769 KB) | DWG (2 MB) | KMZ (2 MB) | SHP (1 MB) | XLSX (601 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/building-age");
                insertDataset(++i, 7, "Heritage Register", "Official listing of properties deemed to have heritage value.", "http://opendata.newwestcity.ca/datasets/heritage-register");
                insertDataset(++i, 7, "Heritage Resource Inventory", "Complete unofficial listing of properties deemed to have heritage value, demolished and standing buildings.", "http://opendata.newwestcity.ca/datasets/heritage-resource-inventory");
                insertDataset(++i, 8, "Block Reference File", "\nThe blocks correspond to a division of the City into about 400 blocks, set up by the City Planner in about 1970. The purpose of these geographic descriptions was to enable more rapid tallying of information by subareas of the City. The geographic subdivision keys would provide easier selection of which properties to include in a run for a report without having to rely on property folio designations which are subject to change, consolidation and subdivision.\nBlock reference file used with \"Historical Development Statistics\" and \"Landuse Percentages by Block\" datasets\n\n\n			SHP (35 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/block-reference-file");
                insertDataset(++i, 8, "Building Attributes", "\nBuilding development specifics including the number of floors above and below ground, the number of residential units, square footage, size of the footprint and site coverage, and address. \n\n\n			CSV (734 KB) | DWG (2 MB) | KMZ (2 MB) | SHP (1 MB) | XLSX (673 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/building-attributes");
                insertDataset(++i, 8, "Building Footprints", "\nOutlines of buildings.  All primary buildings such as residential and commerical are included.\n\n\n			CSV (504 KB) | DWG (1 MB) | KMZ (1 MB) | SHP (939 KB) | XLSX (406 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/building-footprints");
                insertDataset(++i, 8, "Community Conversation on Housing Comments (Our City 2014)", "\nGot to https://www.newwestcity.ca/ourcity for more details\n\n\n			CSV (634 KB) | XLS (2 MB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/community-conversation-on-housing-comments");
                insertDataset(++i, 8, "Contours", "\n1 meter intervals\n\n\n			DWG (11 MB) | KMZ (5 MB) | SHP (3 MB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/contours");
                insertDataset(++i, 8, "Historical Development Statistics", "\nStatistics per Hectare show a sample of four types of uses or content in a particular block/area; the number of residences, the number of buildings, the floor space ratio which is the ratio of a buildings total floor area to the size of the land upon which it is built, and the number of parking spaces on property in the area. \nUse in conjunction with the Block Reference Dataset.\n\n\n			CSV (3 MB)\n	\n\n", "");
                insertDataset(++i, 8, "Land Use", "Land use represents what a parcel of land is currently being used for (i.e., the land parcel’s primary use). The land use shown in this layer does not necessarily reflect the…\n\n", "http://opendata.newwestcity.ca/datasets/landuse");
                insertDataset(++i, 8, "Land Use Industrial", "Land use represents what a parcel of land is currently being used for (i.e., the land parcel’s primary use). The land use shown in this layer does not necessarily reflect the…\n\n", "http://opendata.newwestcity.ca/datasets/land-use-industrial");
                insertDataset(++i, 8, "Landuse Percentages by block", "\nThe Landuse Percentages show what proportion of a block/area has a particular use (e.g., being used for Commercial purposes or Single Family Residences). \nUse in conjunction with the Block Reference Dataset\n\n\n			CSV (4 MB)\n	\n\n", "");
                insertDataset(++i, 8, "Orthophotography", "Aerial photography over the City of New Westminster\n\n", "http://opendata.newwestcity.ca/datasets/aerial-photography-2012");
                insertDataset(++i, 8, "Parcel Blocks", "\nBlock outlines of contiguous aggregated parcels. \n\n\n			DWG (808 KB) | KMZ (1 MB) | SHP (943 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/parcel-blocks");
                insertDataset(++i, 8, "Parcels", "\nNo Description Available\n\n\n			CSV (218 KB) | DWG (2 MB) | KMZ (1 MB) | SHP (800 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/parcel-polygons");
                insertDataset(++i, 8, "Projects on the Go", "Current applications for rezoning, development permit and heritage revitalization agreement projects currently being processed by the City, including application status…\n\n", "http://opendata.newwestcity.ca/datasets/projects-on-the-go");
                insertDataset(++i, 8, "Zoning", "The City of New Westminster Zoning Bylaw No. 6680 was adopted by Council September 17, 2001. Subsequent amendments to the Zoning Bylaw are consolidated for convenience only. For…\n\n", "http://opendata.newwestcity.ca/datasets/zoning");
                insertDataset(++i, 9, "Accessible Public Washrooms", "Listing of all the accessible washrooms that are available within the City.\n\n", "http://opendata.newwestcity.ca/datasets/accessible-public-washroomsAddresses");
                insertDataset(++i, 11, "Bike Routes", "This dataset contains bike routes including planned and current bikeways, on-street and off-street, as well as dedicated lanes.\n\n", "http://opendata.newwestcity.ca/datasets/parks-bikeways");
                insertDataset(++i, 12, "Drinking Fountains", "\n\n", "http://opendata.newwestcity.ca/datasets/drinking-fountains");
                insertDataset(++i, 9, "Greenways", "Metadata | DWG | KMZ | SHP", "http://opendata.newwestcity.ca/datasets/parks-major-greenways");
                insertDataset(++i, 9, "Off Leash Dog Areas", "\n\n", "http://opendata.newwestcity.ca/datasets/off-leash-dog-areas");
                insertDataset(++i, 9, "Park Benches and Dedications", "Park benches locations throughout the city.", "http://opendata.newwestcity.ca/datasets/park-benches");
                insertDataset(++i, 9, "Park Greenspaces", "\n\n", "http://opendata.newwestcity.ca/datasets/parks-greenspaces");
                insertDataset(++i, 9, "Park Structures", "Park Structure data will include the structure name, structure type, quantity, monument dedication inscription, furnishing photo graph (as available) park name and/or location.…\n\n", "http://opendata.newwestcity.ca/datasets/park-structures");
                insertDataset(++i, 9, "Park Trails", "\n\n", "http://opendata.newwestcity.ca/datasets/park-trails");
                insertDataset(++i, 9, "Parks", "\n\n", "http://opendata.newwestcity.ca/datasets/parks");
                insertDataset(++i, 9, "Public Art", "\n\n", "http://opendata.newwestcity.ca/datasets/public-art");
                insertDataset(++i, 9, "Tree Inventory - East", "Street trees do more than beautify our City and create community pride. Street trees have been scientifically proven to: save energy by reduce heating or cooling costs for…\n\n", "http://opendata.newwestcity.ca/datasets/tree-inventory-east");
                insertDataset(++i, 9, "Tree Inventory - West", "Street trees do more than beautify our City and create community pride. Street trees have been scientifically proven to: save energy by reduce heating or cooling costs for…\n\n", "http://opendata.newwestcity.ca/datasets/tree-inventory-west");
                insertDataset(++i, 10, "Emergency Incidents By Fire Hall", "\nEmergency incident summary counts for past five years by hall. The total monthly and yearly calls are represented.\n\n\n			CSV (483 B) | CSV (483 B) | CSV (483 B) | CSV (519 B) | CSV (519 B) | XLSX (24 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/emergency-incidents-by-fire-hall");
                insertDataset(++i, 10, "Emergency Incidents By Fire Hall Summary", "\nEmergency incident summary counts for past five years by hall. The total monthly and yearly calls are represented.\n\n\n			CSV (483 B) | CSV (483 B) | CSV (483 B) | CSV (519 B) | CSV (519 B) | XLSX (24 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/emergency-incidents-by-fire-hall-summary");
                insertDataset(++i, 10, "Emergency Incidents by Type (Fire and Rescue Services)", "\nIncident types by month/year.  The total and percentage of incidents of total calls is also represented.  Only the current year is available in .csv.\n\n\n			CSV (926 B) | XLSX (27 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/fire-incidents-by-type");
                insertDataset(++i, 10, "Fire and Rescue Services Buildings", "\n\n", "http://opendata.newwestcity.ca/datasets/significant-buildings-fire");
                insertDataset(++i, 10, "Fire Incidents by Year", "\nFire incidents by year.  The total number of incidents is broken down into reportable to the Office of the Fire Commissioner and non reportable.\n\n\n			CSV (206 B) | XLSX (9 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/fire-incidents-by-year");
                insertDataset(++i, 10, "Hospital Buildings", "\n\n", "http://opendata.newwestcity.ca/datasets/significant-buildings-hospitals");
                insertDataset(++i, 10, "Oil Tanks (Removed/Decommissioned)", "\nThe number of underground storage tanks that are active, removed, or outstanding in the removal process, by year.\n\n\n			CSV (294 B) | XLSX (11 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/removed-decommissioned-oil-tanks");
                insertDataset(++i, 10, "Police Buildings", "\n\n", "http://opendata.newwestcity.ca/datasets/significant-buildings-police");
                insertDataset(++i, 11, "Alternative Fuels and Electric Charging Stations", "Electric vehicles are an environmentally friendly mode of transportation. As cleaner emission vehicles gain momentum across the lower mainland, the City of New Westminster is…\n\n", "http://opendata.newwestcity.ca/datasets/electric-charging-stations");
                insertDataset(++i, 11, "ICBC Crash Data", "\nLower mainland crashes\nSee how many crashes are happening at intersections in New Westminster and around the Lower Mainland. \nClick here to visit the ICBC Lower Mainland Crash website page!\n\n\n			No Downloads Available\n	\n\n", "http://opendata.newwestcity.ca/datasets/icbc-crash-data");
                insertDataset(++i, 11, "Intersections", "The junctions at-grade of two or more roads either meeting or crossing.\n\n", "http://opendata.newwestcity.ca/datasets/intersections");
                insertDataset(++i, 11, "Parking Pay Stations", "Identifies the locations of all multi-space digital pay stations for parking in the City.\n\n", "http://opendata.newwestcity.ca/datasets/parking-pay-stations");
                insertDataset(++i, 11, "Railways", "\n\n", "http://opendata.newwestcity.ca/datasets/railways");
                insertDataset(++i, 11, "School Walking Routes", "Walking to school promotes healthy and safe communities benefiting children, families, and the earth.\n\n", "http://opendata.newwestcity.ca/datasets/school-walking-routes");
                insertDataset(++i, 11, "SkyTrain Centreline", "\n\n", "http://opendata.newwestcity.ca/datasets/skytrain-centreline");
                insertDataset(++i, 11, "SkyTrain Stations", "SkyTrain Stations within New Westminster shown as point locations.\n\n", "http://opendata.newwestcity.ca/datasets/skytrain-stations");
                insertDataset(++i, 11, "SkyTrain Stations Points", "SkyTrain Stations within New Westminster shown as point locations.\n\n", "http://opendata.newwestcity.ca/datasets/skytrain-stations-points");
                insertDataset(++i, 11, "Street Features", "\n\n", "http://opendata.newwestcity.ca/datasets/street-features");
                insertDataset(++i, 11, "Street Names", "\nList of all current in-use street names used within the City. \n\n\n			CSV (4 KB) | XLSX (7 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/street-names");
                insertDataset(++i, 11, "Street Network", "Street centerlines and road classification\n\n", "http://opendata.newwestcity.ca/datasets/street-network");
                insertDataset(++i, 11, "Traffic Controllers/Signals", "\n\n", "http://opendata.newwestcity.ca/datasets/traffic-controllers");
                insertDataset(++i, 11, "Traffic Volumes", "Traffic volume counts at midblock points between the years 2006 and 2016 inclusive.\n\n", "http://opendata.newwestcity.ca/datasets/traffic-volumes");
                insertDataset(++i, 11, "Truck Routes", "\n\n", "http://opendata.newwestcity.ca/datasets/truck-routes");
                insertDataset(++i, 11, "Webcam Links", "\nActive webcam locations in New Westminster. \n\n\n			CSV (1 KB) | XLSX (11 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/webcam-links");
                insertDataset(++i, 11, "Wheelchair Ramps", "\n\n", "http://opendata.newwestcity.ca/datasets/wheelchair-ramps");
                insertDataset(++i, 12, "Sewer Catchbasins", "\n\n", "http://opendata.newwestcity.ca/datasets/sewer-catchbasins");
                insertDataset(++i, 12, "Sewer Culverts", "\n\n", "http://opendata.newwestcity.ca/datasets/sewer-culverts");
                insertDataset(++i, 12, "Sewer Ditches", "\n\n", "http://opendata.newwestcity.ca/datasets/sewer-ditches");
                insertDataset(++i, 12, "Sewer Mains", "\n\n", "http://opendata.newwestcity.ca/datasets/sewer-mains");
                insertDataset(++i, 12, "Sewer Maintenance Holes", "\n\n", "http://opendata.newwestcity.ca/datasets/sewer-maintenance-holes");
                insertDataset(++i, 12, "Water Hydrants", "A hydrant is an outlet from a fluid main often consisting of an upright pipe with a valve attached from which fluid (e.g. water or fuel) can be tapped.\n\n", "http://opendata.newwestcity.ca/datasets/water-hydrants");
                insertDataset(++i, 12, "Water Pressure Zones", "This polygon feature class represents each water pressure zone in the City of New Westminster water distribution system.", "http://opendata.newwestcity.ca/datasets/water-pressure-zones");
                insertDataset(++i, 12, "Water Quality Data", "\nA hydrant is an outlet from a fluid main often consisting of an upright pipe with a valve attached from which fluid (e.g. water or fuel) can be tapped. This data set presents the raw data from which our Annual Water Quality report is generated.  For full context for the data please refer to the report.\nNWR Comp 2015.xlsm - Monthly bacteriological analysis of portable water samples\nNWR Numbers 2015.xlsm - Monthly samples for coliform bacteria\nNWR By-station 2015.xlsm - Full year water quality testing by station (addresses given are locations of the sampling station)\nNWR HPC 2015.xlsm - Monthly heterotrophic plate count\nNWR 4Q DBP.xlsm - 4th quarter disinfectant by product reports\n\n\n			XLSM (30 KB) | XLSM (75 KB) | XLSM (27 KB) | XLSX (44 KB) | XLSX (13 KB)\n	\n\n", "http://opendata.newwestcity.ca/datasets/water-quality-data");
                insertDataset(++i, 12, "Water Valves", "A device that regulates the flow of water.\n\n", "http://opendata.newwestcity.ca/datasets/water-valves");
                insertDataset(++i, 12, "Watermains", "A principal pipe in a system of pipes for conveying water, especially one installed underground.\n\n", "http://opendata.newwestcity.ca/datasets/watermains");


                // These are recently added... Instead of hard-coding them like the above, I enabled a sync task from CategoriesActivity to update the DB
                // from opendata.newwestcity.ca
                /*
                insertDataset(++i, , "Bus Routes", "", "http://opendata.newwestcity.ca/datasets/bus-routes");
                insertDataset(++i, , "Bus Stops", "", "http://opendata.newwestcity.ca/datasets/bus-stops");
                insertDataset(++i, , "Business Licenses (Inter-Municipal) ", "", "http://opendata.newwestcity.ca/datasets/inter-muncipal-business-licenses");
                insertDataset(++i, , "Demographic Profiles (Census 1986", "", "1991");
                insertDataset(++i, , "Financial Disclosure Statements", "", "http://opendata.newwestcity.ca/datasets/financial-disclosure-statements");
                insertDataset(++i, , "Historical Development Statistics ", "", "http://opendata.newwestcity.ca/datasets/historical-development-statistics");
                insertDataset(++i, , "Landuse Percentages by block ", "", "http://opendata.newwestcity.ca/datasets/landuse-percentage-by-block");
                insertDataset(++i, , "Playgrounds", "", "http://opendata.newwestcity.ca/datasets/playgrounds");
                insertDataset(++i, , "Regulatory Signs", "", "http://opendata.newwestcity.ca/datasets/regulatory-signs");
                insertDataset(++i, , "Speed Signs", "", "http://opendata.newwestcity.ca/datasets/speed-signs");
                insertDataset(++i, , "Sports Fields", "", "http://opendata.newwestcity.ca/datasets/sports-fields");
                insertDataset(++i, , "Survey Monuments", "", "http://opendata.newwestcity.ca/datasets/survey-monuments");
                insertDataset(++i, , "Warning Signs", "", "http://opendata.newwestcity.ca/datasets/warning-signs");
                */


                db.setTransactionSuccessful();
            }
            finally
            {
                db.endTransaction();
            }
        }

        db.close();

        return i;
    }


}
