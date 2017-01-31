package pl.kksionek.docchecker.model;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import pl.kksionek.docchecker.R;
import pl.kksionek.docchecker.data.Doc;
import pl.kksionek.docchecker.data.DocId;

public class DocRecyclerAdapter extends RecyclerView.Adapter<DocRecyclerAdapter.DocViewHolder> {

    private static final SimpleDateFormat sDateFormatter = new SimpleDateFormat(
            "dd.MM.yyyy HH:mm:ss", Locale.getDefault());

    private final LinkedHashMap<String, Doc> mHashMap = new LinkedHashMap<>();

    @Override
    public DocViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item, parent, false);
        return new DocViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DocViewHolder holder, int position) {
        Doc document = mHashMap.values().toArray(new Doc[0])[position];

        if (document instanceof DocId) {
            holder.typeIcon.setImageResource(R.drawable.ic_contact_mail_black_24dp);
        } else {
            holder.typeIcon.setImageResource(R.drawable.ic_travel_passport);
        }

        holder.title.setText(document.getNumber());
        holder.reqStatus.setText(document.getReqStatus());
        holder.docStatus.setText(document.getDocStatus());
        String message = document.getMessage();
        holder.statusIcon.setImageResource(
                (message != null && message.contains("jest gotowy do odbioru"))
                        ? R.drawable.ic_approve : R.drawable.ic_info);
        holder.message.setText(document.getMessage());
        holder.timestamp.setText(sDateFormatter.format(new Date(document.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return mHashMap.size();
    }

    public void update(Doc newDoc) {
        int i = 0;
        for (Map.Entry<String, Doc> entry : mHashMap.entrySet()) {
            if (entry.getKey().equals(newDoc.getNumber())) {
                mHashMap.put(newDoc.getNumber(), newDoc);
                notifyItemChanged(i);
                return;
            }
            ++i;
        }

        mHashMap.put(newDoc.getNumber(), newDoc);
        notifyItemInserted(mHashMap.size() - 1);
    }

    public String remove(int adapterPosition) {
        int i = 0;
        for (Map.Entry<String, Doc> entry : mHashMap.entrySet()) {
            if (i != adapterPosition) {
                ++i;
                continue;
            }
            String key = entry.getKey();
            mHashMap.remove(key);
            notifyItemRemoved(adapterPosition);
            return key;
        }
        return null;
    }

    public class DocViewHolder extends RecyclerView.ViewHolder {

        ImageView typeIcon;
        TextView title;
        TextView reqStatus;
        TextView docStatus;
        ImageView statusIcon;
        TextView message;
        TextView timestamp;

        public DocViewHolder(View itemView) {
            super(itemView);
            typeIcon = (ImageView) itemView.findViewById(R.id.recycler_item_doc_type_image);
            title = (TextView) itemView.findViewById(R.id.recycler_item_title);
            reqStatus = (TextView) itemView.findViewById(R.id.recycler_item_req_status_text);
            docStatus = (TextView) itemView.findViewById(R.id.recycler_item_doc_status_text);
            statusIcon = (ImageView) itemView.findViewById(R.id.recycler_item_doc_status_icon);
            message = (TextView) itemView.findViewById(R.id.recycler_item_message_text);
            timestamp = (TextView) itemView.findViewById(R.id.recycler_item_doc_timestamp);
        }
    }
}
