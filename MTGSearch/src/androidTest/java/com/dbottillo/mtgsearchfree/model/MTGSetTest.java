package com.dbottillo.mtgsearchfree.model;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MTGSetTest {

    MTGSet set;

    @Before
    public void setup() {
        set = new MTGSet(100);
        set.setName("Zendikar");
        set.setCode("ZEN");
    }

    @Test
    public void mtgSet_ParcelableWriteRead() {
        Parcel parcel = Parcel.obtain();
        set.writeToParcel(parcel, set.describeContents());
        parcel.setDataPosition(0);

        MTGSet createdFromParcel = MTGSet.CREATOR.createFromParcel(parcel);
        assertThat(createdFromParcel, is(set));
    }
}