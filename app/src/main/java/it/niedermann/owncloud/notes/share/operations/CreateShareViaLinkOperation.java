package it.niedermann.owncloud.notes.share.operations;

import java.util.ArrayList;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.shares.CreateShareRemoteOperation;
import com.owncloud.android.lib.resources.shares.OCShare;
import com.owncloud.android.lib.resources.shares.ShareType;


import it.niedermann.owncloud.notes.shared.user.User;

/**
 * Creates a new public share for a given file
 */
public class CreateShareViaLinkOperation extends SyncOperation {

    private final String path;
    private final String password;
    private int permissions = OCShare.NO_PERMISSION;

    public CreateShareViaLinkOperation(String path, String password, User user) {
        super(user);

        this.path = path;
        this.password = password;
    }

    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        CreateShareRemoteOperation createOp = new CreateShareRemoteOperation(path,
                ShareType.PUBLIC_LINK,
                "",
                false,
                password,
                permissions);
        createOp.setGetShareDetails(true);
        RemoteOperationResult result = createOp.execute(client);

        if (result.isSuccess()) {
            if (result.getData().size() > 0) {
                Object item = result.getData().get(0);
                if (item instanceof OCShare) {
                    updateData((OCShare) item);
                } else {
                    ArrayList<Object> data = result.getData();
                    result = new RemoteOperationResult(RemoteOperationResult.ResultCode.SHARE_NOT_FOUND);
                    result.setData(data);
                }
            } else {
                result = new RemoteOperationResult(RemoteOperationResult.ResultCode.SHARE_NOT_FOUND);
            }
        }

        return result;
    }

    private void updateData(OCShare share) {
        // Update DB with the response
        share.setPath(path);
        if (path.endsWith(FileUtils.PATH_SEPARATOR)) {
            share.setFolder(true);
        } else {
            share.setFolder(false);
        }

        // TODO: Save share

        /*
        getStorageManager().saveShare(share);

        // Update OCFile with data from share: ShareByLink  and publicLink
        OCFile file = getStorageManager().getFileByEncryptedRemotePath(path);
        if (file != null) {
            file.setSharedViaLink(true);
            getStorageManager().saveFile(file);
        }
         */

    }

    public String getPath() {
        return this.path;
    }

    public String getPassword() {
        return this.password;
    }
}
