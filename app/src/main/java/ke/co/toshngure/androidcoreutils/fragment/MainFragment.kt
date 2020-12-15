package ke.co.toshngure.androidcoreutils.fragment

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import ke.co.toshngure.androidcoreutils.R
import ke.co.toshngure.androidcoreutils.api.TestApiService
import ke.co.toshngure.androidcoreutils.database.AppDatabase
import ke.co.toshngure.androidcoreutils.users.RegisterUserResponse
import ke.co.toshngure.basecode.app.BaseAppFragment
import ke.co.toshngure.basecode.paging.sync.SyncStatesDatabase
import ke.co.toshngure.extensions.executeAsync
import kotlinx.android.synthetic.main.fragment_main.*
import retrofit2.Call

class MainFragment : BaseAppFragment<RegisterUserResponse>() {


    override fun onSetUpContentView(container: FrameLayout) {
        super.onSetUpContentView(container)
        layoutInflater.inflate(R.layout.fragment_main, container, true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        usersBtn.setOnClickListener { navigateWithPermissionsCheck(R.id.usersFragment) }

        postsBtn.setOnClickListener { navigateWithPermissionsCheck(R.id.postsFragment) }

        albumsBtn.setOnClickListener { navigateWithPermissionsCheck(R.id.albumsFragment) }

        imagesPickerBtn.setOnClickListener {
        //makeRequest()
        navigateWithPermissionsCheck(
                R.id.testImagesPickerFragment, null,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }

        smsRetrieverBtn.setOnClickListener { navigateWithPermissionsCheck(R.id.smsRetrieverFragment) }

        clearFab.setOnClickListener {
            executeAsync {

                AppDatabase.getInstance().clearAllTables()

                SyncStatesDatabase.getInstance().clearAllTables()
            }
        }

        gpxBtn.setOnClickListener {
            navigateWithPermissionsCheck(
                R.id.gpxFragment, null,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }

        cacheTestBtn.setOnClickListener { navigateWithPermissionsCheck(R.id.cacheTestFragment) }

        sharedPrefsBtn.setOnClickListener { navigateWithPermissionsCheck(R.id.sharedPrefsFragment) }

        logsBtn.setOnClickListener { navigateWithPermissionsCheck(R.id.fragment_logs) }
    }

    companion object {
        const val TAG = "MainFragment"
    }

    override fun getApiCall(): Call<RegisterUserResponse>? {
        return TestApiService.getTypicodeInstance().register("gogesmwaura1@yahoo.co.uk", "pass1234",  "pass1234", "254722333867", "Good Luck", "")
    }

    override fun onDataReady(data: RegisterUserResponse) {
        super.onDataReady(data)
    }
}
